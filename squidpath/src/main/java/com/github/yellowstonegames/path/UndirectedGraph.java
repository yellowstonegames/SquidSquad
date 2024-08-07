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

import com.github.yellowstonegames.grid.Coord;

import java.util.Collection;

/**
 * A kind of {@link Graph} where all connections between vertices are two-way and have equal cost for traveling A to B
 * or B to A.
 * @see DefaultGraph The DefaultGraph class supports the common case where V is Coord and all costs are 1.
 * @param <V> the vertex type; often {@link Coord}
 * @author earlygrey
 */
public class UndirectedGraph<V> extends Graph<V>{
    protected UndirectedGraphAlgorithms<V> algorithms;

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


    //================================================================================
    // Graph building
    //================================================================================

    @Override
    protected Connection<V> obtainEdge() {
        return new Connection.UndirectedConnection<>();
    }

    @Override
    protected Connection<V> addConnection(Node<V> a, Node<V> b, float weight) {
        Connection<V> e = a.addEdge(b, weight);
        edgeSet.add(e);
        b.addEdge(a, weight);
        return e;
    }

    @Override
    protected boolean removeConnection(Node<V> a, Node<V> b) {
        Connection<V> e = a.removeEdge(b);
        if (e == null) return false;
        b.removeEdge(a);
        edgeSet.remove(e);
        return true;
    }

    @Override
    protected Connection<V> getEdge(Node<V> a, Node<V> b) {
        Connection<V> edge = a.getEdge(b);
        if (edge == null) return null;
        edge = edgeSet.get(edge);
        return edge;
    }


    //================================================================================
    // Superclass implementations
    //================================================================================

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    protected Graph<V> createNew() {
        return new UndirectedGraph<>();
    }

    @Override
    public UndirectedGraphAlgorithms<V> algorithms() {
        return algorithms;
    }

    @Override
    public String toString() {
        return "UndirectedGraph" + vertexMap.keySet();
    }
}
