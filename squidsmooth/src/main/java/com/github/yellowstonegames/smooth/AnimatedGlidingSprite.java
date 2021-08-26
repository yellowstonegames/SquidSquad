package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.yellowstonegames.grid.Coord;

/**
 * Extends {@link Sprite}, but uses an {@link Animation} of {@link TextureRegion} for its visuals and a
 * {@link CoordGlider} to store its position. The {@link CoordGlider} is publicly available as {@link #location} or with
 * {@link #getLocation()}, which should be used to determine or change where this started its move, where it is going,
 * and how far it has gone between the two. You Must Call {@link #animate(float)} with an increasing float parameter
 * when you want the animation to be playing; otherwise it will stay on the first frame (or a later frame if you stop
 * calling animate() at some other point). You can use the {@link VectorSequenceGlider} {@link #smallMotion} to move
 * this Sprite at a finer resolution than between Coords for start and end points.
 * <br>
 * You probably want to use Textures with a width and height of 1 world unit in
 * your Animation, and {@link #setSize(float, float)} on this to {@code 1, 1}; this avoids the need to convert between
 * Coord units in the CoordGlider and some other unit in the world.
 */
public class AnimatedGlidingSprite extends Sprite {
    public Animation<? extends TextureRegion> animation;
    public CoordGlider location;
    public VectorSequenceGlider smallMotion;
    protected final VectorSequenceGlider ownEmptyMotion = VectorSequenceGlider.EMPTY.copy();

    private AnimatedGlidingSprite()
    {
        super();
    }
    public AnimatedGlidingSprite(Animation<? extends TextureRegion> animation) {
        super();
        this.animation = animation;
        setRegion(animation.getKeyFrame(0f));
        smallMotion = ownEmptyMotion;
    }

    public AnimatedGlidingSprite(Animation<? extends TextureRegion> animation, Coord coord) {
        this(animation, coord, coord);
    }

    public AnimatedGlidingSprite(Animation<? extends TextureRegion> animation, Coord start, Coord end) {
        super();
        this.animation = animation;
        setRegion(animation.getKeyFrame(0f));
        location = new CoordGlider(start, end);
        smallMotion = ownEmptyMotion;
    }

    /**
     * Required to use to have the animation play; give this a steadily increasing stateTime (measured in seconds, as a
     * float) and it will steadily play the animation; if stateTime stops increasing or this stops being called, then
     * the animation is effectively paused.
     * @param stateTime time playing the animation, in seconds; usually not an exact integer
     * @return this for chaining
     */
    public AnimatedGlidingSprite animate(final float stateTime)
    {
        setRegion(animation.getKeyFrame(stateTime));
        return this;
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

    public Animation<? extends TextureRegion> getAnimation() {
        return animation;
    }

    public void setAnimation(Animation<? extends TextureRegion> animation) {
        this.animation = animation;
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
