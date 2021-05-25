package com.github.yellowstonegames.glyph;

import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.smooth.CoordGlider;

/**
 * A single {@code long} that a {@link Font} can render as a glyph with color and styles, given a location that can
 * smoothly change as a {@link CoordGlider}.
 */
public class GlidingGlyph {
    public CoordGlider location;
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
    }

    public float getX()
    {
        return location.getX();
    }

    public float getY()
    {
        return location.getY();
    }

    public CoordGlider getLocation() {
        return location;
    }

    public void setLocation(CoordGlider location) {
        this.location = location;
    }

    public long getGlyph() {
        return glyph;
    }

    public void setGlyph(long glyph) {
        this.glyph = glyph;
    }
}
