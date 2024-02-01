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
package com.github.yellowstonegames.path.sg;

import com.github.tommyettinger.ds.ObjectDeque;

import java.util.Collection;

public class Deque<T> extends ObjectDeque<T> {
    public Deque() {
        super();
    }

    public Deque(int initialSize) {
        super(initialSize);
    }


    public Deque(int capacity, boolean resize) {
        super(capacity);
        if (resize) this.size = capacity;
    }

    public Deque(Collection<? extends T> coll) {
        super(coll);
    }

    public Deque(ObjectDeque<? extends T> deque) {
        super(deque);
    }

    public Deque(T[] a) {
        super(a);
    }

    public Deque(T[] a, int offset, int count) {
        super(a, offset, count);
    }


}
