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

import java.util.ArrayDeque;

import com.github.yellowstonegames.path.sg.Deque;
import com.github.yellowstonegames.path.sg.Connection;
import com.github.yellowstonegames.path.sg.Node;
import com.github.yellowstonegames.path.sg.utils.SearchProcessor;

public class BreadthFirstSearch<V> extends Algorithm<V> {

    private SearchProcessor<V> processor;
    private SearchStep<V> step = new SearchStep<>();

    private final ArrayDeque<Node<V>> queue;

    public BreadthFirstSearch(int id, Node<V> start, SearchProcessor<V> processor) {
        super(id);
        this.processor = processor;
        queue = new ArrayDeque<>();
        start.resetAlgorithmAttribs(id);
        queue.add(start);
        start.setSeen(true);
    }

    @Override
    public boolean update() {
        if (isFinished()) return true;

        Node<V> v = queue.pollFirst();
        if (processor != null) {
            step.prepare(v);
            processor.accept(step);
            if (step.terminate) {
                queue.clear();
                return true;
            }
            if (step.ignore) return isFinished();
        }
        Deque<Connection<V>> outEdges = v.getOutEdges();
        for (Connection<V> e : outEdges) {
            Node<V> w = e.getNodeB();
            w.resetAlgorithmAttribs(id);
            if (!w.isSeen()) {
                w.setIndex(v.getIndex() + 1);
                w.setDistance(v.getDistance() + e.getWeight());
                w.setConnection(e);
                w.setSeen(true);
                queue.addLast(w);
            }
        }
        return isFinished();
    }

    @Override
    public boolean isFinished() {
        return queue.isEmpty();
    }

}
