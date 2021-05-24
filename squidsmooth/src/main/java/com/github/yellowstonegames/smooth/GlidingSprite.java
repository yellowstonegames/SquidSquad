package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.yellowstonegames.grid.Coord;

/**
 * Extends {@link Sprite}, using a normal {@link TextureRegion} its visuals and a {@link CoordGlider} to store its
 * position. The {@link CoordGlider} is publicly available as {@link #location} or with {@link #getLocation()}, which
 * should be used to determine or change where this started its move, where it is going, and how far it has gone between
 * the two. You probably want to use a Texture with a width and height of 1 world unit, and
 * {@link #setSize(float, float)} on this to {@code 1, 1}; this avoids the need to convert between Coord units in the
 * CoordGlider and some other unit in the world.
 */
public class GlidingSprite extends Sprite {
    public CoordGlider location;

    private GlidingSprite()
    {
        super();
    }
    public GlidingSprite(TextureRegion region) {
        super();
        setRegion(region);
        setSize(1f, 1f);
    }

    public GlidingSprite(TextureRegion region, Coord coord) {
        this(region, coord, coord);
    }

    public GlidingSprite(TextureRegion region, Coord start, Coord end) {
        super();
        setRegion(region);
        location = new CoordGlider(start, end);
        setSize(1f, 1f);
    }

    public float getX()
    {
        return location.getX();
    }

    public float getY()
    {
        return location.getY();
    }
    @Override
    public float[] getVertices() {
        super.setPosition(location.getX(), location.getY());
        return super.getVertices();
    }

    public CoordGlider getLocation() {
        return location;
    }

    public void setLocation(CoordGlider location) {
        this.location = location;
    }
}
