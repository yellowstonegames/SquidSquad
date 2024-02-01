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

import com.github.yellowstonegames.path.sg.Connection.UndirectedConnection;
import com.github.yellowstonegames.path.sg.algorithms.UndirectedGraphAlgorithms;
import com.github.yellowstonegames.path.sg.utils.WeightFunction;

public class UndirectedGraph<V> extends Graph<V> {

    UndirectedGraphAlgorithms<V> algorithms;

    //================================================================================
    // Constructors
    //================================================================================

    public UndirectedGraph() {
        super();
        algorithms = new UndirectedGraphAlgorithms<>(this);
    }

    public UndirectedGraph(Collection<V> vertices) {
        super(vertices);
        algorithms = new UndirectedGraphAlgorithms<>(this);
    }

    public UndirectedGraph(Graph<V> graph) {
        super(graph);
        algorithms = new UndirectedGraphAlgorithms<>(this);
    }


    //================================================================================
    // Graph building
    //================================================================================

    @Override
    protected UndirectedConnection<V> obtainEdge() {
        return new UndirectedConnection<>();
    }

    @Override
    Connection<V> addConnection(Node<V> a, Node<V> b, WeightFunction<V> weight) {
        Connection<V> e = a.getEdge(b);
        if (e == null) {
            UndirectedConnection<V> e1 = obtainEdge(), e2 = obtainEdge();
            e1.set(a, b, weight);
            e2.set(b, a, weight);
            a.addEdge(e1);
            b.addEdge(e2);
            edges.add(e1);
            e = e1;
        } else {
            e.setWeight(weight);
        }
        return e;
    }

    @Override
    Connection<V> addConnection(Node<V> a, Node<V> b) {
        Connection<V> e = a.getEdge(b);
        return e != null ? edges.get(e) : addConnection(a, b, getDefaultEdgeWeightFunction());
    }

    @Override
    boolean removeConnection(Node<V> a, Node<V> b) {
        Connection<V> e = a.removeEdge(b);
        if (e == null) return false;
        b.removeEdge(a);
        edges.remove(e);
        return true;
    }

    @Override
    Connection<V> getEdge(Node<V> a, Node<V> b) {
        Connection<V> edge = a.getEdge(b);
        return edge == null ? null : edges.get(edge); // get from map to ensure consistent instance is returned
    }


    //================================================================================
    // Superclass implementations
    //================================================================================

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public UndirectedGraph<V> createNew() {
        return new UndirectedGraph<>();
    }

    @Override
    public UndirectedGraphAlgorithms<V> algorithms() {
        return algorithms;
    }


    //================================================================================
    // Misc
    //================================================================================

    /**
     * @return the degree of this vertex, or -1 if it is not in the graph
     */
    public int getDegree(V v) {
        Node<V> node = getNode(v);
        return node == null ? -1 : node.getOutDegree();
    }
}
