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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

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
    }
}
