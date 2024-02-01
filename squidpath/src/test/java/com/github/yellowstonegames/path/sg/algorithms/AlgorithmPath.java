/*
 * Copyright (c) 2024 See AUTHORS file.
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

package com.github.yellowstonegames.path.sg.algorithms;

import com.github.yellowstonegames.path.sg.Node;
import com.github.yellowstonegames.path.sg.Path;

class AlgorithmPath<V> extends Path<V> {

    AlgorithmPath() {
        super(0, false);
    }

    AlgorithmPath(Node<V> v) {
        super(v.getIndex() + 1, true);
        setByBacktracking(v);
    }

    void setByBacktracking(Node<V> node) {
        int nodeCount = node.getIndex() + 1;

        if (values.length < nodeCount) resize(nodeCount);

        Node<V> v = node;
        while(v != null) {
            set(v.getIndex(), v.getObject());
            v = v.getPrev();
        }

        setLength(node.getDistance());
    }
}
