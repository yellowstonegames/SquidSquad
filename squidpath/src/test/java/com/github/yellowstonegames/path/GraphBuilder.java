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
package com.github.yellowstonegames.path;


import com.github.tommyettinger.ds.ObjectObjectMap;

public final class GraphBuilder {

    private GraphBuilder() {
    }

    public static <V, G extends Graph<V>> void buildCompleteGraph(G graph) {
        ObjectObjectMap.Values<V, Node<V>> values1 = new ObjectObjectMap.Values<>(graph.vertexMap);
        for (Node<V> a : values1) {
            ObjectObjectMap.Values<V, Node<V>> values2 = new ObjectObjectMap.Values<>(graph.vertexMap);
            for (Node<V> b : values2) {
                if (!a.equals(b)) {
                    Connection<V> e = a.getEdge(b);
                    if (e == null) {
                        graph.addConnection(a, b);
                    }
                    if (graph.isDirected()) {
                        e = b.getEdge(a);
                        if (e == null) {
                            graph.addConnection(b, a);
                        }
                    }
                }
            }
        }
    }

}
