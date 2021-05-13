package com.github.yellowstonegames.smooth.dawnlike;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.smooth.CoordGlider;

/**
 * Like a {@link com.badlogic.gdx.graphics.g2d.Sprite}, but lighter-weight and customized to the conventions of this
 * demo. Has a start and end position that it is expected to move between as its {@link CoordGlider#getChange()} field
 * changes. Supports a packed int color.
 * <br>
 * Created by Tommy Ettinger on 12/20/2019.
 */
public class AnimatedGlider extends Sprite {
    public Animation<TextureRegion> animation;
    CoordGlider position;

    private AnimatedGlider()
    {
        super();
    }
    public AnimatedGlider(Animation<TextureRegion> animation) {
        super();
        this.animation = animation;
        setRegion(animation.getKeyFrame(0f));
        setSize(1f, 1f);
    }

    public AnimatedGlider(Animation<TextureRegion> animation, Coord coord) {
        this(animation, coord, coord);
    }

    public AnimatedGlider(Animation<TextureRegion> animation, Coord start, Coord end) {
        super();
        this.animation = animation;
        setRegion(animation.getKeyFrame(0f));
        position = new CoordGlider(start, end);
        setSize(1f, 1f);
    }

    public AnimatedGlider animate(final float stateTime)
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
