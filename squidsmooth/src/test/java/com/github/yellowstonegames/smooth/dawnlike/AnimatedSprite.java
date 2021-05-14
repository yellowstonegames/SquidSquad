package com.github.yellowstonegames.smooth.dawnlike;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.smooth.CoordGlider;

/**
 * Extends {@link Sprite}, but is lighter-weight and customized to the conventions of this demo. Has a
 * {@link CoordGlider} publicly available as {@link #position}, which should be used to determine or change
 * where this started its move, where it is going, and how far it has gone between the two.
 * <br>
 * Created by Tommy Ettinger on 12/20/2019.
 */
public class AnimatedSprite extends Sprite {
    public Animation<TextureRegion> animation;
    public CoordGlider position;

    private AnimatedSprite()
    {
        super();
    }
    public AnimatedSprite(Animation<TextureRegion> animation) {
        super();
        this.animation = animation;
        setRegion(animation.getKeyFrame(0f));
        setSize(1f, 1f);
    }

    public AnimatedSprite(Animation<TextureRegion> animation, Coord coord) {
        this(animation, coord, coord);
    }

    public AnimatedSprite(Animation<TextureRegion> animation, Coord start, Coord end) {
        super();
        this.animation = animation;
        setRegion(animation.getKeyFrame(0f));
        position = new CoordGlider(start, end);
        setSize(1f, 1f);
    }

    public AnimatedSprite animate(final float stateTime)
    {
        setRegion(animation.getKeyFrame(stateTime));
        return this;
    }
    
    public float getX()
    {
        return position.getX();
    }

    public float getY()
    {
        return position.getY();
    }
    @Override
    public float[] getVertices() {
        super.setPosition(position.getX(), position.getY());
        return super.getVertices();
    }

}
