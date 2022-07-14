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

package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.yellowstonegames.grid.Coord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Extends {@link Sprite}, using a normal {@link TextureRegion} its visuals and a {@link CoordGlider} to store its
 * position. The {@link CoordGlider} is publicly available as {@link #location} or with {@link #getLocation()}, which
 * should be used to determine or change where this started its move, where it is going, and how far it has gone between
 * the two. You probably want to use a Texture with a width and height of 1 world unit, because by default this calls
 * {@code setSize(1, 1);} in its constructor; this avoids the need to convert between Coord units in the CoordGlider and
 * some other unit in the world.
 */
public class GlidingSprite extends Sprite {
    @Nonnull
    public CoordGlider location;
    @Nonnull
    public VectorSequenceGlider smallMotion;
    /**
     * A VectorSequenceGlider that is empty (has no motions) and belongs to this AnimatedGlidingSprite.
     * This is public so external code can use it, but should never be modified.
     * It is here so {@link #smallMotion} can be easily set to an empty sequence.
     * You can also use {@code setSmallMotion(null)} to stop any small motion.
     */
    @Nonnull
    public final VectorSequenceGlider ownEmptyMotion = VectorSequenceGlider.EMPTY.copy();

    private GlidingSprite()
    {
        this(null);
    }
    public GlidingSprite(TextureRegion region) {
        this(region, Coord.get(0, 0));
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

    @Nonnull
    public CoordGlider getLocation() {
        return location;
    }

    public void setLocation(@Nonnull CoordGlider location) {
        this.location = location;
    }

    @Nonnull
    public VectorSequenceGlider getSmallMotion() {
        return smallMotion;
    }

    public void setSmallMotion(@Nullable VectorSequenceGlider smallMotion) {
        if(smallMotion == null) this.smallMotion = ownEmptyMotion;
        else this.smallMotion = smallMotion;
    }
}
