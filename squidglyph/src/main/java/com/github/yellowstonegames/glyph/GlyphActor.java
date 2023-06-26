/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.tommyettinger.textra.ColorLookup;
import com.github.tommyettinger.textra.Font;
import com.github.yellowstonegames.grid.Coord;

/**
 * A simple Actor that carries with it just one {@code long} of glyph info and a {@link Font} to show that glyph with.
 * You can slide, scale, or rotate this like any other Actor, often using
 * {@link com.badlogic.gdx.scenes.scene2d.actions.Actions} or the expansions in {@link MoreActions}.
 */
public class GlyphActor extends Actor {

    public long glyph;
    public Font font;

    public GlyphActor() {
        this(0xFFFFFFFE00000000L | '@', null);
    }

    public GlyphActor(long g, Font f) {
        glyph = g;
        font = f;
    }

    public GlyphActor(char c, Font f) {
        glyph = 0xFFFFFFFE00000000L | c;
        font = f;
    }

    public GlyphActor(char c, int color, Font f) {
        glyph = ((long) color << 32 & 0xFFFFFFFE00000000L) | c;
        font = f;
    }

    public GlyphActor(char c, String markup, Font f) {
        glyph = f.markupGlyph(c, markup);
        font = f;
    }

    /**
     * You should usually use this constructor if you want to place an inline image (such as an emoji) as a GlyphActor.
     * @param markup must contain at least one char that will be displayed; you can use {@code [+🤴🏽]} syntax to enter an emoji, if supported
     * @param f a Font, which should have had images added to it if you want to show inline images
     */
    public GlyphActor(String markup, Font f) {
        glyph = f.markupGlyph(markup);
        font = f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.getColor().set((int)(glyph >>> 32)).a *= parentAlpha;
        batch.setColor(batch.getColor());
        font.drawGlyph(batch, glyph, getX(), getY() - font.descent * font.scaleY, getRotation(), getScaleX(), getScaleY());
    }

    public void setColor(int color) {
        super.getColor().set(color);
        super.setColor(super.getColor());
        glyph = (glyph & 0xFFFFFFFFL) | ((long) color << 32 & 0xFFFFFFFE00000000L);
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        glyph = (glyph & 0xFFFFFFFFL) | ((long) Color.rgba8888(color) << 32 & 0xFFFFFFFE00000000L);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        super.setColor(r, g, b, a);
        glyph = (glyph & 0xFFFFFFFFL) | ((long) Color.rgba8888(super.getColor()) << 32 & 0xFFFFFFFE00000000L);
    }

    @Override
    public Color getColor() {
        Color.rgba8888ToColor(super.getColor(), (int) (glyph >>> 32));
        return super.getColor();
    }

    public char getChar() {
        return (char) glyph;
    }

    public void setChar(char c) {
        glyph = (glyph & 0xFFFFFFFFFFFF0000L) | c;
    }

    public long getGlyph() {
        return glyph;
    }

    public void setGlyph(long glyph) {
        this.glyph = glyph;
    }

    /**
     * You can set the complete glyph here using markup, including the possibility of setting the glyph to an inline
     * image (such as an emoji) using the {@code [+🤴🏽]} syntax.
     * @param markup a markup String using square-bracket markup; must have at least one visible char
     */
    public void setWithMarkup(String markup) {
        this.glyph = font.markupGlyph(markup);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Coord getLocation() {
        return Coord.get(Math.round(getX()), Math.round(getY()));
    }

    public void setLocation(Coord location) {
        setPosition(location.x, location.y);
    }
}
