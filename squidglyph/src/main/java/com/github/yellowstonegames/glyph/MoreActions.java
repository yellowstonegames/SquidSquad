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

package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.Direction;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.action;

/**
 * Supplemental {@link Action} classes and methods to augment {@link Actions}.
 */
public class MoreActions {

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

}
