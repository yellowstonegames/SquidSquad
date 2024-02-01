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

import java.util.Collection;
import java.util.Set;

import com.github.yellowstonegames.path.sg.utils.WeightFunction;

public class Internals<V> {

    final Graph<V> graph;

    Internals(Graph<V> graph) {
        this.graph = graph;
    }

    public Node<V> getNode(V v) {
        return graph.getNode(v);
    }

    public Collection<Node<V>> getNodes() {
        return graph.nodeMap.nodeCollection;
    }

    public Set<Connection<V>> getConnections() {
        return graph.edges;
    }

    public void addConnection(Node<V> a, Node<V> b, WeightFunction<V> weightFunction) {
        graph.addConnection(a, b, weightFunction);
    }
}
