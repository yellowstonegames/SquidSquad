/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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

import com.badlogic.gdx.math.Vector2;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.grid.Direction;

import static com.badlogic.gdx.math.Vector2.Zero;
import static com.github.yellowstonegames.grid.Direction.*;

/**
 * Very experimental; allows chaining a sequence of VectorGlider movements.
 */
@Beta
public class VectorSequenceGlider extends SequenceGlider {

    /**
     * A VectorSequenceGlider that has no movements; useful as a default value. You probably want to copy this using
     * {@link #copy()} instead of modifying this global reference, though there aren't many changes that will make a
     * difference in the behavior of this particular global.
     */
    public static final VectorSequenceGlider EMPTY = new VectorSequenceGlider();

    /**
     * Predefined VectorSequenceGliders for bump animations in different directions (keyed by that direction). It is strongly
     * recommended that you copy the VectorSequenceGlider value using {@link #copy()}, and avoid using it directly.
     */
    public static final ObjectObjectOrderedMap<Direction, VectorSequenceGlider> BUMPS =
            ObjectObjectOrderedMap.with(
                    UP,         new VectorSequenceGlider(new VectorGlider[]{new VectorGlider(new Vector2(0, 0.5f)), new VectorGlider(new Vector2(0, 0.5f), Zero)}, new float[]{3f, 5f}),
                    UP_RIGHT,   new VectorSequenceGlider(new VectorGlider[]{new VectorGlider(new Vector2(0.35f, 0.35f)), new VectorGlider(new Vector2(0.35f, 0.35f), Zero)}, new float[]{3f, 5f}),
                    RIGHT,      new VectorSequenceGlider(new VectorGlider[]{new VectorGlider(new Vector2(0.5f, 0f)), new VectorGlider(new Vector2(0.5f, 0f), Zero)}, new float[]{3f, 5f}),
                    DOWN_RIGHT, new VectorSequenceGlider(new VectorGlider[]{new VectorGlider(new Vector2(0.35f, -0.35f)), new VectorGlider(new Vector2(0.35f, -0.35f), Zero)}, new float[]{3f, 5f}),
                    DOWN,       new VectorSequenceGlider(new VectorGlider[]{new VectorGlider(new Vector2(0, -0.5f)), new VectorGlider(new Vector2(0, -0.5f), Zero)}, new float[]{3f, 5f}),
                    DOWN_LEFT,  new VectorSequenceGlider(new VectorGlider[]{new VectorGlider(new Vector2(-0.35f, -0.35f)), new VectorGlider(new Vector2(-0.35f, -0.35f), Zero)}, new float[]{3f, 5f}),
                    LEFT,       new VectorSequenceGlider(new VectorGlider[]{new VectorGlider(new Vector2(-0.5f, 0f)), new VectorGlider(new Vector2(-0.5f, 0f), Zero)}, new float[]{3f, 5f}),
                    UP_LEFT,    new VectorSequenceGlider(new VectorGlider[]{new VectorGlider(new Vector2(-0.35f, 0.35f)), new VectorGlider(new Vector2(-0.35f, 0.35f), Zero)}, new float[]{3f, 5f})
            );

    public VectorSequenceGlider(){
        this(new VectorGlider[0], new float[0]);
    }
    public VectorSequenceGlider(VectorGlider[] gliders, float[] lengths) {
        super(gliders, lengths);
    }

    public VectorSequenceGlider(VectorSequenceGlider other){
        super(new VectorGlider[other.sequence.length], new float[other.durations.length]);
        for (int i = 0; i < other.sequence.length; i++) {
            sequence[i] = new VectorGlider(other.sequence[i]);
            durations[i] = other.durations[i];
        }
        this.active = other.active;
        this.change = other.change;
        this.passed = other.passed;
        this.interpolation = other.interpolation;
        this.completeRunner = other.completeRunner;
    }

    public VectorSequenceGlider copy(){
        return new VectorSequenceGlider(this);
    }

    public float getX()
    {
        if(sequence.length == 0) return 0;
        if(active >= sequence.length)
            return sequence[sequence.length - 1].getEndFloat("x");
        if(sequence[active].change >= 1f)
            return sequence[active].getEndFloat("x");
        return interpolation.apply(sequence[active].getStartFloat("x"), sequence[active].getEndFloat("x"), sequence[active].change);
    }

    public float getY()
    {
        if(sequence.length == 0) return 0;
        if(active >= sequence.length)
            return sequence[sequence.length - 1].getEndFloat("y");
        if(sequence[active].change >= 1f)
            return sequence[active].getEndFloat("y");
        return interpolation.apply(sequence[active].getStartFloat("y"), sequence[active].getEndFloat("y"), sequence[active].change);
    }
}
