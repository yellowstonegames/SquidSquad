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
import java.util.Collections;

import com.github.yellowstonegames.path.sg.Connection;
import com.github.yellowstonegames.path.sg.Connection.DirectedConnection;
import com.github.yellowstonegames.path.sg.Edge;
import com.github.yellowstonegames.path.sg.Graph;
import com.github.yellowstonegames.path.sg.Node;
import com.github.yellowstonegames.path.sg.algorithms.DirectedGraphAlgorithms;

public class DirectedGraph<V> extends Graph<V> {

    final DirectedGraphAlgorithms<V> algorithms;

    //================================================================================
    // Constructors
    //================================================================================

    public DirectedGraph () {
        super();
        algorithms = new DirectedGraphAlgorithms<>(this);
    }

    public DirectedGraph (Collection<V> vertices) {
        super(vertices);
        algorithms = new DirectedGraphAlgorithms<>(this);
    }

    public DirectedGraph(Graph<V> graph) {
        super(graph);
        algorithms = new DirectedGraphAlgorithms<>(this);
    }

    //================================================================================
    // Superclass implementations
    //================================================================================

    @Override
    protected Connection<V> obtainEdge() {
        return new DirectedConnection<>();
    }

    @Override
    public DirectedGraph<V> createNew() {
        return new DirectedGraph<>();
    }

    @Override
    public DirectedGraphAlgorithms<V> algorithms() {
        return algorithms;
    }


    //================================================================================
    // Misc
    //================================================================================

    /**
     * @return the out degree of this vertex, or -1 if it is not in the graph
     */
    public int getOutDegree(V v) {
        Node<V> node = getNode(v);
        return node == null ? -1 : node.getOutDegree();
    }

    /**
     * @return the in degree of this vertex, or -1 if it is not in the graph
     */
    public int getInDegree(V v) {
        Node<V> node = getNode(v);
        return node == null ? -1 : node.getInDegree();
    }

    /**
     * Get a collection containing all the edges which have v as a head.
     * That is, for every edge e in the collection, e = (u, v) for some vertex u.
     * @param v the vertex which all edges will have as a head
     * @return an unmodifiable collection of edges
     */
    public Collection<Edge<V>> getInEdges(V v) {
        Node<V> node = getNode(v);
        if (node==null) return null;
        return Collections.unmodifiableCollection(node.getInEdges());
    }

    /**
     * Sort the vertices of this graph in topological order. That is, for every edge from vertex u to vertex v, u comes before v in the ordering.
     * This is reflected in the iteration order of the collection returned by {@link Graph#getVertices()}.
     * Note that the graph cannot contain any cycles for a topological order to exist. If a cycle exists, this method will do nothing.
     * @return true if the sort was successful, false if the graph contains a cycle
     */
    public boolean topologicalSort() {
        return nodeMap.topologicalSort();
    }


}
