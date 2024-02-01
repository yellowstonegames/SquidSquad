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

import java.util.HashSet;
import java.util.Set;

import com.github.yellowstonegames.path.sg.Deque;
import com.github.yellowstonegames.path.sg.Connection;
import com.github.yellowstonegames.path.sg.Graph;
import com.github.yellowstonegames.path.sg.Node;

public class CycleDetector<V> extends Algorithm<V> {

    private boolean containsCycle;

    protected CycleDetector(int id, Graph<V> graph) {
        super(id);
        containsCycle = findCycle(graph);
    }

    @Override
    public boolean update() {
        return true;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    boolean findCycle(Graph<V> graph) {
        if (graph.size() < 3 || graph.getEdgeCount() < 3) return false;
        int runID = graph.algorithms().requestRunID();
        for (Node<V> v : graph.internals().getNodes()) {
            v.resetAlgorithmAttribs(runID);
            if (detectCycleDFS(v, null, new HashSet<>(), runID, graph)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectCycleDFS(Node<V> v, Node<V> parent, Set<Node<V>> recursiveStack, int runID, Graph<V> graph) {
        v.setProcessed(true);
        recursiveStack.add(v);
        Deque<Connection<V>> outEdges = v.getOutEdges();
        for (Connection<V> e : outEdges) {
            Node<V> u = e.getNodeB();
            if (!graph.isDirected() && u.equals(parent)) continue;
            u.resetAlgorithmAttribs(runID);
            if (recursiveStack.contains(u)) {
                return true;
            }
            if (!u.isProcessed()) {
                if (detectCycleDFS(u, v, recursiveStack, runID, graph)) return true;
            }
        }
        recursiveStack.remove(v);
        return false;
    }

    public boolean containsCycle() {
        return containsCycle;
    }
}
