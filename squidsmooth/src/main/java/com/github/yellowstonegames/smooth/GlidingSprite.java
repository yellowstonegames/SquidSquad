package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.yellowstonegames.grid.Coord;

/**
 * Extends {@link Sprite}, using a normal {@link TextureRegion} its visuals and a {@link CoordGlider} to store its
 * position. The {@link CoordGlider} is publicly available as {@link #location} or with {@link #getLocation()}, which
 * should be used to determine or change where this started its move, where it is going, and how far it has gone between
 * the two. You probably want to use a Texture with a width and height of 1 world unit, because by default this calls
 * {@code setSize(1, 1);} in its constructor; this avoids the need to convert between Coord units in the CoordGlider and
 * some other unit in the world.
 */
public class GlidingSprite extends Sprite {
    public CoordGlider location;
    public VectorSequenceGlider smallMotion;

    protected final VectorSequenceGlider ownEmptyMotion = VectorSequenceGlider.EMPTY.copy();

    private GlidingSprite()
    {
        super();
    }
    public GlidingSprite(TextureRegion region) {
        super();
        setRegion(region);
        setSize(1f, 1f);
        smallMotion = ownEmptyMotion;
    }

    public GlidingSprite(TextureRegion region, Coord coord) {
        this(region, coord, coord);
    }

    public GlidingSprite(TextureRegion region, Coord start, Coord end) {
        super();
        setRegion(region);
        location = new CoordGlider(start, end);
        setSize(1f, 1f);
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
    @Override
    public float[] getVertices() {
        super.setPosition(getX(), getY());
        return super.getVertices();
    }

    public CoordGlider getLocation() {
        return location;
    }

    public void setLocation(CoordGlider location) {
        this.location = location;
    }

    public VectorSequenceGlider getSmallMotion() {
        return smallMotion;
    }

    public void setSmallMotion(VectorSequenceGlider smallMotion) {
        if(smallMotion == null) this.smallMotion = ownEmptyMotion;
        else this.smallMotion = smallMotion;
    }
}
