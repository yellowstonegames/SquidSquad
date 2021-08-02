package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.smooth.AngleGlider;
import com.github.yellowstonegames.smooth.CoordGlider;
import com.github.yellowstonegames.smooth.FloatGlider;

import javax.annotation.Nonnull;

/**
 * A single {@code long} that a {@link Font} can render as a glyph with color and styles, given a location that can
 * smoothly change as a {@link CoordGlider}. May optionally have an unchanging or a changing rotation as an
 * {@link AngleGlider}.
 */
public class GlidingGlyph {
    @Nonnull
    public CoordGlider location;
    @Nonnull
    public AngleGlider rotation;
    public long glyph;

    private GlidingGlyph() {
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
    }

    public GlidingGlyph(long glyph, Coord start, Coord end, float rotation) {
        this.glyph = glyph;
        location = new CoordGlider(start, end);
        this.rotation = new AngleGlider(rotation);
    }

    public GlidingGlyph(long glyph, Coord start, Coord end, float rotationStart, float rotationEnd) {
        this.glyph = glyph;
        location = new CoordGlider(start, end);
        rotation = new AngleGlider(rotationStart, rotationEnd);
    }

    public float getX()
    {
        return location.getX();
    }

    public float getY()
    {
        return location.getY();
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
        font.drawGlyph(batch, glyph, location.getX(), location.getY(), rotation.getAngle());
    }
}
