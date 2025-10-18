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

package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.digital.Interpolations.Interpolator;
import com.github.yellowstonegames.grid.Direction;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.action;

/**
 * Supplemental {@link Action} classes and methods to augment {@link Actions}.
 */
public final class MoreActions {
    /**
     * No need to instantiate.
     */
    private MoreActions() {
    }

    /**
     * Just like {@link ParallelAction}, but if given null Action items, it ignores them.
     */
    public static class LenientParallelAction extends ParallelAction {
        public LenientParallelAction() {
            super();
        }

        public LenientParallelAction(Action action1) {
            super(action1);
        }

        public LenientParallelAction(Action action1, Action action2) {
            super(action1, action2);
        }

        public LenientParallelAction(Action action1, Action action2, Action action3) {
            super(action1, action2, action3);
        }

        public LenientParallelAction(Action action1, Action action2, Action action3, Action action4) {
            super(action1, action2, action3, action4);
        }

        public LenientParallelAction(Action action1, Action action2, Action action3, Action action4, Action action5) {
            super(action1, action2, action3, action4, action5);
        }

        /**
         * This is exactly like {@link ParallelAction#addAction(Action)}, but if given a null Action, it does nothing
         * instead of potentially crashing later.
         * @param action may be null, or may be an Action that will run in parallel
         */
        @Override
        public void addAction(Action action) {
            if(action == null) return;
            super.addAction(action);
        }
    }

    /**
     * Executes a number of actions one at a time. This is exactly like
     * {@link com.badlogic.gdx.scenes.scene2d.actions.SequenceAction}, but ignores null Action items given to it.
     * @author Nathan Sweet
     */
    public static class LenientSequenceAction extends LenientParallelAction {
        private int index;

        public LenientSequenceAction() {
        }

        public LenientSequenceAction(Action action1) {
            addAction(action1);
        }

        public LenientSequenceAction(Action action1, Action action2) {
            addAction(action1);
            addAction(action2);
        }

        public LenientSequenceAction(Action action1, Action action2, Action action3) {
            addAction(action1);
            addAction(action2);
            addAction(action3);
        }

        public LenientSequenceAction(Action action1, Action action2, Action action3, Action action4) {
            addAction(action1);
            addAction(action2);
            addAction(action3);
            addAction(action4);
        }

        public LenientSequenceAction(Action action1, Action action2, Action action3, Action action4, Action action5) {
            addAction(action1);
            addAction(action2);
            addAction(action3);
            addAction(action4);
            addAction(action5);
        }

        public boolean act (float delta) {
            Array<Action> actions = getActions();
            if (index >= actions.size) return true;
            Pool pool = getPool();
            setPool(null); // Ensure this action can't be returned to the pool while executing.
            try {
                if (actions.get(index).act(delta)) {
                    if (actor == null) return true; // This action was removed.
                    index++;
                    if (index >= actions.size) return true;
                }
                return false;
            } finally {
                setPool(pool);
            }
        }

        public void restart () {
            super.restart();
            index = 0;
        }

        /**
         * Appends a Runnable to run after the rest of the sequence. If {@code runnable} is null, simply returns this
         * without changes.
         * @param after may be null (in which case nothing changes), otherwise will be run after the sequence
         * @return this, for chaining
         */
        public LenientSequenceAction append(Action after) {
            if(after == null) return this;
            addAction(after);
            return this;
        }

        /**
         * Appends a Runnable to run at the conclusion of the sequence. If {@code runnable} is null, simply returns this
         * without changes.
         * @param runnable may be null (in which case nothing changes), otherwise will be run after the sequence
         * @return this, for chaining
         */
        public LenientSequenceAction conclude(Runnable runnable) {
            if(runnable == null) return this;
            addAction(Actions.run(runnable));
            return this;
        }
    }

    static {
        // required since libGDX 1.14.0
        Actions.ACTION_POOLS.addPool(LenientParallelAction::new);
        Actions.ACTION_POOLS.addPool(LenientSequenceAction::new);
    }

    public static LenientSequenceAction sequence (Action action1) {
        LenientSequenceAction action = action(LenientSequenceAction.class);
        action.addAction(action1);
        return action;
    }

    public static LenientSequenceAction sequence (Action action1, Action action2) {
        LenientSequenceAction action = action(LenientSequenceAction.class);
        action.addAction(action1);
        action.addAction(action2);
        return action;
    }

    public static LenientSequenceAction sequence (Action action1, Action action2, Action action3) {
        LenientSequenceAction action = action(LenientSequenceAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        return action;
    }

    public static LenientSequenceAction sequence (Action action1, Action action2, Action action3, Action action4) {
        LenientSequenceAction action = action(LenientSequenceAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        return action;
    }

    public static LenientSequenceAction sequence (Action action1, Action action2, Action action3, Action action4, Action action5) {
        LenientSequenceAction action = action(LenientSequenceAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        action.addAction(action5);
        return action;
    }

    public static LenientSequenceAction sequence (Action... actions) {
        LenientSequenceAction action = action(LenientSequenceAction.class);
        for (int i = 0, n = actions.length; i < n; i++)
            action.addAction(actions[i]);
        return action;
    }

    public static LenientSequenceAction sequence () {
        return action(LenientSequenceAction.class);
    }

    public static LenientParallelAction parallel (Action action1) {
        LenientParallelAction action = action(LenientParallelAction.class);
        action.addAction(action1);
        return action;
    }

    public static LenientParallelAction parallel (Action action1, Action action2) {
        LenientParallelAction action = action(LenientParallelAction.class);
        action.addAction(action1);
        action.addAction(action2);
        return action;
    }

    public static LenientParallelAction parallel (Action action1, Action action2, Action action3) {
        LenientParallelAction action = action(LenientParallelAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        return action;
    }

    public static LenientParallelAction parallel (Action action1, Action action2, Action action3, Action action4) {
        LenientParallelAction action = action(LenientParallelAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        return action;
    }

    public static LenientParallelAction parallel (Action action1, Action action2, Action action3, Action action4, Action action5) {
        LenientParallelAction action = action(LenientParallelAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        action.addAction(action5);
        return action;
    }

    public static LenientParallelAction parallel (Action... actions) {
        LenientParallelAction action = action(LenientParallelAction.class);
        for (int i = 0, n = actions.length; i < n; i++)
            action.addAction(actions[i]);
        return action;
    }

    public static LenientParallelAction parallel () {
        return action(LenientParallelAction.class);
    }


    public static LenientSequenceAction bump(Direction way, float duration) {
        return sequence(
                Actions.moveBy(way.deltaX * 0.4f, way.deltaY * 0.4f, duration * 0.3f),
                Actions.moveBy(way.deltaX * -0.4f, way.deltaY * -0.4f, duration * 0.7f)
        );
    }

    public static LenientSequenceAction bump(Direction way, float duration, Runnable post) {
        return bump(way, duration).conclude(post);
    }

    public static DelayAction bump(Direction way, float duration, float delaySeconds, Runnable post) {
        return Actions.delay(delaySeconds, bump(way, duration).conclude(post));
    }

    public static LenientSequenceAction bump(float degrees, float duration) {
        float cos = TrigTools.cosDeg(degrees), sin = TrigTools.sinDeg(degrees);
        return sequence(
                Actions.moveBy(cos *  0.4f, sin *  0.4f, duration * 0.3f),
                Actions.moveBy(cos * -0.4f, sin * -0.4f, duration * 0.7f)
        );
    }

    public static LenientSequenceAction bump(float degrees, float duration, Runnable post) {
        return bump(degrees, duration).conclude(post);
    }

    public static DelayAction bump(float degrees, float duration, float delaySeconds, Runnable post) {
        return Actions.delay(delaySeconds, bump(degrees, duration).conclude(post));
    }

    public static MoveToAction slideTo(float x, float y, float duration) {
        return Actions.moveTo(x, y, duration);
    }

    public static LenientSequenceAction slideTo(float x, float y, float duration, Runnable post) {
        return sequence(slideTo(x, y, duration), Actions.run(post));
    }

    public static DelayAction slideTo(float x, float y, float duration, float delaySeconds, Runnable post) {
        return Actions.delay(delaySeconds, slideTo(x, y, duration, post));
    }

    public static MoveByAction slideBy(float x, float y, float duration) {
        return Actions.moveBy(x, y, duration);
    }

    public static LenientSequenceAction slideBy(float x, float y, float duration, Runnable post) {
        return sequence(slideBy(x, y, duration), Actions.run(post));
    }

    public static DelayAction slideBy(float x, float y, float duration, float delaySeconds, Runnable post) {
        return Actions.delay(delaySeconds, slideBy(x, y, duration, post));
    }

    public static LenientSequenceAction wiggle(float strength, float duration) {
        long time = System.currentTimeMillis() << 2;
        float x1 =   TrigTools.sinTurns(Hasher.randomize1Float(time)) * strength,
                y1 = TrigTools.sinTurns(Hasher.randomize1Float(time+1)) * strength,
                x2 = TrigTools.sinTurns(Hasher.randomize1Float(time+2)) * strength,
                y2 = TrigTools.sinTurns(Hasher.randomize1Float(time+3)) * strength;
        return sequence(
                Actions.moveBy(x1, y1, duration * 0.25f),
                Actions.moveBy(x2, y2, duration * 0.25f),
                Actions.moveBy(-x2, -y1, duration * 0.25f),
                Actions.moveBy(-x1, -y2, duration * 0.25f)
        );
    }

    public static LenientSequenceAction wiggle(float strength, float duration, Runnable post) {
        return wiggle(strength, duration).conclude(post);
    }

    public static DelayAction wiggle(float strength, float duration, float delaySeconds, Runnable post) {
        return Actions.delay(delaySeconds, bump(strength, duration).conclude(post));
    }

    /**
     * Meant to be used with {@link GlyphGrid#summon(float, float, float, float, float, char, int, int, float, float, float, Interpolation, Runnable)}
     * as its {@code moveRunnable}, this makes a summoned glyph take an "arc-like" path toward the target, where it is
     * fast at the beginning and end of its motion and reaches the height of its arc at the center. This effect can also
     * be achieved with {@link Interpolations#biasGainFunction(float, float)} when its shape is less than 1.0, and with
     * BiasGain you can change where the height of the arc is by raising or lowering the turning parameter. Using
     * arcMoveInterpolation is still simplest.
     */
    public static final Interpolator arcMoveInterpolation = new Interpolator("arcMove", a -> {
        if (a <= 0.5f) return (1 - ((float)Math.pow(2f, -3f * (a * 2)) - 0.125f) * 1.1428572f) * 0.5f;
        return (1 + (float) Math.pow(2f, 3f * (a * 2 - 2)) - 0.25f) * 0.5714286f;
    });

    // This is here as commented-out code because there are probably links to this section of code somewhere, and even
    // though I use Interpolator now and not libGDX Interpolation, this could still be useful to code that does need an
    // Interpolation. Code here should use Interpolations#biasGainFunction(float, float) .
//    /**
//     * A wrapper around {@link MathTools#barronSpline(float, float, float)} to use it as an Interpolation.
//     * Useful because it can imitate the wide variety of symmetrical Interpolations by setting turning to 0.5 and shape
//     * to some value greater than 1, while also being able to produce the inverse of those interpolations by setting
//     * shape to some value between 0 and 1.
//     */
//    public static class BiasGain extends Interpolation {
//        /**
//         * The shape parameter will cause this to imitate "smoothstep-like" splines when greater than 1 (where the
//         * values ease into their starting and ending levels), or to be the inverse when less than 1 (where values
//         * start like square root does, taking off very quickly, but also end like square does, landing abruptly at
//         * the ending level).
//         */
//        public final float shape;
//        /**
//         * A value between 0.0 and 1.0, inclusive, where the shape changes.
//         */
//        public final float turning;
//
//        /**
//         * Constructs a useful default BiasGain interpolation with a smoothstep-like shape.
//         * This has a shape of 2.0f and a turning of 0.5f .
//         */
//        public BiasGain() {
//            this(2f, 0.5f);
//        }
//
//        /**
//         * Constructs a BiasGain interpolation with the specified (positive) shape and specified turning (between 0 and
//         * 1 inclusive).
//         * @param shape must be positive; similar to a straight line when near 1, becomes smoothstep-like above 1, and
//         *              becomes shaped like transpose of smoothstep below 1
//         * @param turning where, between 0 and 1 inclusive, this should change from the starting curve to the ending one
//         */
//        public BiasGain (float shape, float turning) {
//            this.shape = Math.max(0.0001f, shape);
//            this.turning = Math.min(Math.max(turning, 0f), 1f);
//        }
//
//        /**
//         * The implementation here is the same as it is in other barronSpline() versions; this uses the shape and
//         * turning values configured in the constructor.
//         * @param a between 0 and 1 inclusive
//         * @return a float between 0 and 1 inclusive
//         */
//        public float apply (float a) {
//            final float d = turning - a;
//            final int f = BitConversion.floatToIntBits(d) >> 31, n = f | 1;
//            return (turning * n - f) * (a + f) / (Float.MIN_NORMAL - f + (a + shape * d) * n) - f;
//        }
//    }

}
