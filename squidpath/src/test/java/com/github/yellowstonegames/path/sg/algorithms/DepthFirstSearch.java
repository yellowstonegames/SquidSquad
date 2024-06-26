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

public class DepthFirstSearch<V> extends Algorithm<V> {

    private SearchProcessor<V> processor;
    private SearchStep<V> step = new SearchStep<>();

    private final ArrayDeque<Node<V>> queue;

    public DepthFirstSearch(int id, Node<V> start, SearchProcessor<V> processor) {
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
        //System.out.println("poll " + v);
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
                queue.addFirst(w);
               // System.out.println("add " + w);
            }
        }
       // System.out.println("queue " + queue);
        return isFinished();
    }

    @Override
    public boolean isFinished() {
        return queue.isEmpty();
    }
/*
    void depthFirstSearch(final Node<V> v, final SearchProcessor<V> processor) {
        v.resetAlgorithmAttribs(id);
        v.setDistance(0);
        recursiveDepthFirstSearch(v, processor, 0, processor != null ? new SearchStep<>() : null);
    }

    boolean recursiveDepthFirstSearch(Node<V> v, SearchProcessor<V> processor, int depth, SearchStep<V> step) {
        if (processor != null) {
            step.prepare(v);
            processor.accept(step);
            if (step.terminate) return true;
            if (step.ignore) return false;
        }
        v.setProcessed(true);
        int n = v.getOutEdges().size();
        for (int i = 0; i < n; i++) {
            Connection<V> e = v.getOutEdges().get(i);
            Node<V> w = e.getNodeB();
            w.resetAlgorithmAttribs(id);
            if (!w.isProcessed()) {
                w.setIndex(depth + 1);
                w.setDistance(v.getDistance() + e.getWeight());
                w.setConnection(e);
                if (recursiveDepthFirstSearch(w, processor, depth + 1, step)) {
                    return true;
                }
            }
        }
        return false;
    }*/

}
