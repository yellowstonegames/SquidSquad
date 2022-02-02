/*
 * Copyright (c) 2022 See AUTHORS file.
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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.github.tommyettinger.textra.ColorLookup;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.smooth.AngleGlider;
import com.github.yellowstonegames.smooth.CoordGlider;
import com.github.yellowstonegames.smooth.VectorSequenceGlider;
import com.github.tommyettinger.textra.Font;
import javax.annotation.Nonnull;

/**
 * A single {@code long} that a {@link Font} can render as a glyph with color and styles, given a location that can
 * smoothly change as a {@link CoordGlider}. May optionally have an unchanging or a changing rotation as an
 * {@link AngleGlider}, and can adjust its position between-grid-cells using a {@link VectorSequenceGlider}.
 * This allows constructing a GlidingGlyph with an existing {@code long} as Font uses them, as a char and a String of
 * Font markup (see {@link Font#markupGlyph(char, String, ColorLookup)}), or as a char and several optional parameters.
 */
public class GlidingGlyph {
    @Nonnull
    public CoordGlider location;
    @Nonnull
    public AngleGlider rotation;
    @Nonnull
    public VectorSequenceGlider smallMotion;

    /**
     * A glyph as produced by {@link Font#markupGlyph(char, String, ColorLookup)}, storing a char, color, and styles.
     */
    public long glyph;

    /**
     * A VectorSequenceGlider that is empty (has no motions) and belongs to this GlidingGlyph.
     * This is public so external code can use it, but should never be modified.
     * It is here so {@link #smallMotion} can be easily set to an empty sequence.
     */
    @Nonnull
    public final VectorSequenceGlider ownEmptyMotion = VectorSequenceGlider.EMPTY.copy();

    private GlidingGlyph() {
        this(' ');
    }

    public GlidingGlyph(long glyph) {
        this(glyph, Coord.get(0, 0));
    }

    public GlidingGlyph(long glyph, Coord coord) {
        this(glyph, coord, coord);
    }

    public GlidingGlyph(long glyph, Coord start, Coord end) {
        this.glyph = glyph;
        location = new CoordGlider(start, end);
        rotation = new AngleGlider();
        smallMotion = ownEmptyMotion;
    }

    public GlidingGlyph(long glyph, Coord start, Coord end, float rotation) {
        this.glyph = glyph;
        location = new CoordGlider(start, end);
        this.rotation = new AngleGlider(rotation);
        smallMotion = ownEmptyMotion;
    }

    public GlidingGlyph(long glyph, Coord start, Coord end, float rotationStart, float rotationEnd) {
        this.glyph = glyph;
        location = new CoordGlider(start, end);
        rotation = new AngleGlider(rotationStart, rotationEnd);
        smallMotion = ownEmptyMotion;
    }

    public GlidingGlyph(char glyph, String markup) {
        this(Font.markupGlyph(glyph, markup, GlyphMap::getRgba), Coord.get(0, 0));
    }

    public GlidingGlyph(char glyph, String markup, Coord coord) {
        this(Font.markupGlyph(glyph, markup, GlyphMap::getRgba), coord, coord);
    }

    public GlidingGlyph(char glyph, String markup, Coord start, Coord end) {
        this.glyph = Font.markupGlyph(glyph, markup, GlyphMap::getRgba);
        location = new CoordGlider(start, end);
        rotation = new AngleGlider();
        smallMotion = ownEmptyMotion;
    }

    public GlidingGlyph(char glyph, String markup, Coord start, Coord end, float rotation) {
        this.glyph = Font.markupGlyph(glyph, markup, GlyphMap::getRgba);
        location = new CoordGlider(start, end);
        this.rotation = new AngleGlider(rotation);
        smallMotion = ownEmptyMotion;
    }

    public GlidingGlyph(char glyph, String markup, Coord start, Coord end, float rotationStart, float rotationEnd) {
        this.glyph = Font.markupGlyph(glyph, markup, GlyphMap::getRgba);
        location = new CoordGlider(start, end);
        rotation = new AngleGlider(rotationStart, rotationEnd);
        smallMotion = ownEmptyMotion;
    }

    public GlidingGlyph(char chr, Coord start, Coord end, float rotationStart, float rotationEnd) {
        this.glyph = 0xFFFFFFFF00000000L | chr;
        location = new CoordGlider(start, end);
        rotation = new AngleGlider(rotationStart, rotationEnd);
        smallMotion = ownEmptyMotion;
    }

    public GlidingGlyph(char chr, int rgbaColor, Coord coord) {
        this.glyph = (long) rgbaColor << 32 | chr;
        location = new CoordGlider(coord, coord);
        rotation = new AngleGlider();
        smallMotion = ownEmptyMotion;
    }

    public GlidingGlyph(char chr, int rgbaColor, Coord start, Coord end, float rotationStart, float rotationEnd) {
        this.glyph = (long) rgbaColor << 32 | chr;
        location = new CoordGlider(start, end);
        rotation = new AngleGlider(rotationStart, rotationEnd);
        smallMotion = ownEmptyMotion;
    }

    public GlidingGlyph(char chr, int rgbaColor, boolean bold, boolean oblique, boolean underline,
                        boolean strikethrough, Coord start, Coord end, float rotationStart, float rotationEnd) {
        this.glyph = (long) rgbaColor << 32 | chr | (bold ? Font.BOLD : 0L) | (oblique ? Font.OBLIQUE : 0L)
                | (underline ? Font.UNDERLINE : 0L) | (strikethrough ? Font.STRIKETHROUGH : 0L);
        location = new CoordGlider(start, end);
        rotation = new AngleGlider(rotationStart, rotationEnd);
        smallMotion = ownEmptyMotion;
    }

    public float getX()
    {
        return location.getX() + smallMotion.getX();
    }

    public float getY()
    {
        return location.getY() + smallMotion.getY();
    }

    @Nonnull
    public CoordGlider getLocation() {
        return location;
    }

    public void setLocation(@Nonnull CoordGlider location) {
        this.location = location;
    }

    @Nonnull
    public AngleGlider getRotation() {
        return rotation;
    }

    public void setRotation(@Nonnull AngleGlider rotation) {
        this.rotation = rotation;
    }

    public long getGlyph() {
        return glyph;
    }

    public void setGlyph(long glyph) {
        this.glyph = glyph;
    }

    public void draw(Batch batch, Font font){
        font.drawGlyph(batch, glyph, getX(), getY(), rotation.getAngle());
    }
    @Nonnull
    public VectorSequenceGlider getSmallMotion() {
        return smallMotion;
    }

    public void setSmallMotion(VectorSequenceGlider smallMotion) {
        if(smallMotion == null) this.smallMotion = ownEmptyMotion;
        else this.smallMotion = smallMotion;
    }

}
