/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.tommyettinger.ds.ObjectBag;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.function.ObjPredicate;
import com.github.yellowstonegames.grid.Coord;

import java.util.*;
import java.util.function.Predicate;

/**
 * Abstract superclass of actual Graph types.
 * @param <V> the vertex type; often {@link Coord}
 * @author earlygrey
 */
public abstract class Graph<V> {

    //================================================================================
    // Members
    //================================================================================
    
    protected ObjectObjectOrderedMap<V, Node<V>> vertexMap;
    protected ObjectOrderedSet<Connection<V>> edgeSet;
    
    //================================================================================
    // Constructors
    //================================================================================

    protected Graph() {
        vertexMap = new ObjectObjectOrderedMap<>();
        edgeSet = new ObjectOrderedSet<>();
    }

    protected Graph(Collection<V> vertices) {
        this();
        for (V v : vertices) {
            addVertex(v);
        }
    }

    //================================================================================
    // Graph Builders
    //================================================================================

    //--------------------
    //  Abstract Methods
    //--------------------

    protected abstract Connection<V> obtainEdge();
    protected abstract Graph<V> createNew();
    public abstract Algorithms<V> algorithms();

    //--------------------
    //  Public Methods
    //--------------------

    /**
     * Adds a vertex to the graph.
     * @param v the vertex to be added
     * @return true if the vertex was not already in the graph, false otherwise
     */

    public boolean addVertex(V v) {
        Node<V> node = getNode(v);
        if (node!=null) return false;
        node = new Node<>(v, this);
        vertexMap.put(v, node);
        return true;
    }

    /**
     * Adds all the vertices in the collection to the graph.
     * @param vertices a collection of vertices to be added
     */
    public void addVertices(Collection<V> vertices) {
        for (V v : vertices) {
            addVertex(v);
        }
    }

    /**
     * Adds all the vertices in the array or varargs to the graph.
     * @param vertices an array or varargs of vertices to be added
     */
    @SafeVarargs
    public final void addVertices(V... vertices) {
        for (V v : vertices) {
            addVertex(v);
        }
    }

    /**
     * Removes a vertex from the graph, and any adjacent edges.
     * @param v the vertex to be removed
     * @return true if the vertex was in the graph, false otherwise
     */
    public boolean removeVertex(V v) {
        Node<V> existing = vertexMap.remove(v);
        if (existing==null) return false;
        disconnect(existing);
        return true;
    }

    /**
     * Removes all the vertices in the collection from the graph, and any adjacent edges.
     * @param vertices vertices a collection of vertices to be removed
     */
    public void removeVertices(Collection<V> vertices) {
        for (V v : vertices) {
            removeVertex(v);
        }
    }

    public void removeVertexIf(final ObjPredicate<V> predicate) {
        ObjectBag<V> vertices = new ObjectBag<>(getVertices());
        vertices.removeIf(v -> !predicate.test(v));
        removeVertices(vertices);
    }

    public void disconnect(V v) {
        Node<V> existing = vertexMap.get(v);
        if (existing == null) throw new IllegalArgumentException("No node is present for the given V vertex.");
        disconnect(existing);
    }

    protected void disconnect(Node<V> node) {
        for (int i = node.getConnections().size() - 1; i >= 0; i--) {
            Connection<V> c = node.getConnections().get(i);
            removeConnection(node, c.b);
        }
        if (node.inEdges != null) {
            for (int i = node.inEdges.size() - 1; i >= 0; i--) {
                removeConnection(node.inEdges.get(i).a, node);
            }
        }
        node.disconnect();
    }

    /**
     * Add an edge to the graph, from v to w. The edge will have a default weight of 1.
     * If there is already an edge between v and w, its weight will be set to 1.
     * @param v the source vertex of the edge
     * @param w the destination vertex of the edge
     * @return the edge
     */
    public Connection<V> addEdge(V v, V w) {
        return addEdge(v, w, Connection.DEFAULT_WEIGHT);
    }

    /**
     * Add an edge to the graph, from v to w and with the specified weight.
     * If there is already an edge between v and w, its weight will be set to the specified weight.
     * @param v the source vertex of the edge
     * @param w the destination vertex of the edge
     * @param weight the weight of the edge
     * @return the edge
     */
    public Connection<V> addEdge(V v, V w, float weight) {
        if (v == null || w == null) throw new IllegalArgumentException("Vertices cannot be null");
        if (v.equals(w)) throw new IllegalArgumentException("Self loops are not allowed");
        Node<V> a = getNode(v);
        Node<V> b = getNode(w);
        if (a == null  || b == null) throw new IllegalArgumentException("At least one vertex is not in the graph");
        return addConnection(a, b, weight);
    }

    /**
     * Removes the edge from v to w from the graph.
     * @param v the source vertex of the edge
     * @param w the destination vertex of the edge
     * @return the edge if there exists an edge from v to w, or null if there is no edge
     */
    public boolean removeEdge(V v, V w) {
        Node<V> a = getNode(v), b = getNode(w);
        if (a == null  || b == null) throw new IllegalArgumentException("At least one vertex is not in the graph");
        return removeConnection(a, b);
    }

    public boolean removeEdge(Edge<V> edge) {
        return removeConnection(edge.getInternalNodeA(), edge.getInternalNodeB());
    }

    public void removeEdges(Collection<? extends Edge<V>> edges) {
        for (Edge<V> e : edges) {
            removeConnection(e.getInternalNodeA(), e.getInternalNodeB());
        }
    }

    public void removeEdgeIf(final ObjPredicate<Edge<V>> predicate) {
        ArrayList<Edge<V>> edges = new ArrayList<>(getEdges());
        edges.removeIf(v -> !predicate.test(v));
        removeEdges(edges);
    }

    /**
     * Removes all edges from the graph.
     */
    public void removeAllEdges() {
        for (Node<V> v : getNodes()) {
            v.disconnect();
        }
        edgeSet.clear();
    }

    /**
     * Removes all vertices and edges from the graph.
     */
    public void removeAllVertices() {
        edgeSet.clear();
        vertexMap.clear();
    }

    /**
     * Sort the vertices using the provided comparator. This is reflected in the iteration order of the collection returned
     * by {@link #getVertices()}, as well as algorithms which involve iterating over all vertices.
     * @param comparator a comparator for comparing vertices
     */
    public void sortVertices(Comparator<V> comparator) {
        vertexMap.sort(comparator);
    }

    /**
     * Sort the edges using the provided comparator. This is reflected in the iteration order of the collection returned
     * by {@link #getEdges()}, as well as algorithms which involve iterating over all edges.
     * @param comparator a comparator for comparing edges
     */
    public void sortEdges(Comparator<Connection<V>> comparator) {
        edgeSet.sort(comparator);
    }

    //--------------------
    //  Internal Methods
    //--------------------

    protected void removeNode(Node<V> node) {
        for (int i = node.outEdges.size()-1; i >= 0; i--) {
            removeConnection(node.outEdges.get(i).b, node);
        }
        node.disconnect();
        vertexMap.remove(node.object);
    }

    protected Connection<V> addConnection(Node<V> a, Node<V> b) {
        Connection<V> e = a.addEdge(b, Connection.DEFAULT_WEIGHT);
        edgeSet.add(e);
        return e;
    }

    protected Connection<V> addConnection(Node<V> a, Node<V> b, float weight) {
        Connection<V> e = a.addEdge(b, weight);
        edgeSet.add(e);
        return e;
    }

    protected boolean removeConnection(Node<V> a, Node<V> b) {
        Connection<V> e = a.removeEdge(b);
        if (e == null) return false;
        edgeSet.remove(e);
        return true;
    }

    //================================================================================
    // Getters
    //================================================================================

    //--------------------
    //  Public Getters
    //--------------------

    /**
     * Check if the graph contains a vertex.
     * @param v the vertex with which to check
     * @return true if the graph contains the vertex, false otherwise
     */
    public boolean contains(V v) {
        return vertexMap.containsKey(v);
    }

    /**
     * Retrieve the edge which is from v to w.
     *
     * @param v the source vertex of the edge
     * @param w the destination vertex of the edge
     * @return the edge if it is in the graph, otherwise null
     */
    public Connection<V> getEdge(V v, V w) {
        Node<V> a = getNode(v), b = getNode(w);
        if (a == null  || b == null) throw new IllegalArgumentException("At least one vertex is not in the graph");
        return getEdge(a, b);
    }

    /**
     * Check if the graph contains an edge from v to w.
     * @param v the source vertex of the edge
     * @param w the destination vertex of the edge
     * @return true if the edge is in the graph, false otherwise
     */
    public boolean edgeExists(V v, V w) {
        Node<V> a = getNode(v), b = getNode(w);
        if (a == null  || b == null) throw new IllegalArgumentException("At least one vertex is not in the graph");
        return connectionExists(a, b);
    }

    /**
     * Get a List containing all the edges which have v as a source.
     *
     * @param v the source vertex of all the edges
     * @return a List of edges
     */
    public List<? extends Connection<V>> getEdges(V v) {
        Node<V> node = getNode(v);
        if (node==null) return null;
        return node.outEdges;
    }

    /**
     * Get an ObjectOrderedSet containing all the edges in the graph.
     *
     * @return an ObjectOrderedSet of all the edges in the graph
     */
    public ObjectOrderedSet<? extends Connection<V>> getEdges() {
        return edgeSet;
    }

    /**
     * Get a Set containing all the vertices in the graph.
     * @return a Set of all the vertices in the graph
     */
    public Set<V> getVertices() {
        return vertexMap.keySet();
    }


    /**
     * Check if the graph is directed, that is whether the edges form an ordered pair or a set.
     * @return whether the graph is directed
     */
    public boolean isDirected() {
        return true;
    }

    /**
     * Get the number of vertices in the graph.
     * @return the number of vertices
     */
    public int size() {
        return vertexMap.size();
    }

    /**
     * Get the number of edges in the graph.
     * @return the number of edges
     */
    public int getEdgeCount() {
        return edgeSet.size();
    }


    //--------------------
    //  Internal Getters
    //--------------------

    protected Node<V> getNode(V v) {
        return vertexMap.get(v);
    }

    protected Collection<Node<V>> getNodes() {
        return vertexMap.values();
    }

    protected boolean connectionExists(Node<V> u, Node<V> v) {
        return u.getEdge(v) != null;
    }

    protected Connection<V> getEdge(Node<V> a, Node<V> b) {
        return a.getEdge(b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Graph<?> graph = (Graph<?>) o;

        if (!vertexMap.keySet().equals(graph.vertexMap.keySet())) return false;
        return edgeSet.equals(graph.edgeSet);
    }

    @Override
    public int hashCode() {
        int result = vertexMap.keySet().hashCode();
        result = 31 * result + edgeSet.hashCode();
        return result;
    }
}
