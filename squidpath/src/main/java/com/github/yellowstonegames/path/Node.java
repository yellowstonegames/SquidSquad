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

import com.github.tommyettinger.ds.BinaryHeap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.yellowstonegames.grid.Coord;

import java.util.Collection;

/**
 * An extended version of {@link BinaryHeap.Node} that also stores a reference to the parent Graph,
 * a vertex object of type {@code V}, a Map of neighbor Nodes to the appropriate {@link Connection} per Node, an extra
 * List of those same Connections for faster iteration, and a lot of internal data used by algorithms in this package.
 * @param <V> the vertex type; often {@link Coord}
 * @author earlygrey
 */
public class Node<V> extends BinaryHeap.Node {
    //================================================================================
    // Graph structure related members
    //================================================================================

    protected final Graph<V> graph;
    protected final int idHash;
    protected final V object;
    protected ObjectObjectMap<Node<V>, Connection<V>> neighbors = new ObjectObjectMap<>(4);
    protected ObjectList<Connection<V>> outEdges = new ObjectList<>(4); // ObjectList reuses its iterator, should be fast
    protected ObjectList<Connection<V>> inEdges;

    private static int hashCounter = 12345;

    //================================================================================
    // Constructor
    //================================================================================

    protected Node(V v, Graph<V> graph) {
        super(0f);
        this.object = v;
        this.graph = graph;
        idHash = (hashCounter = hashCounter * 0xDAB ^ 0xBEEFACED); // simple XLCG, won't have GWT problems
        if(graph.isDirected())
            inEdges = new ObjectList<>(4);
    }

    //================================================================================
    // Internal methods
    //================================================================================

    protected Connection<V> getEdge(Node<V> v) {
        return neighbors.get(v);
    }

    protected Connection<V> addEdge(Node<V> v, float weight) {
        Connection<V> edge = neighbors.get(v);
        if (edge == null) {
            edge = graph.obtainEdge();
            edge.set(this, v, weight);
            neighbors.put(v, edge);
            outEdges.add(edge);
            if (v.inEdges != null) v.inEdges.add(edge);
            return edge;
        } else {
            edge.setWeight(weight);
        }
        return edge;
    }
    protected Connection<V> removeEdge(Node<V> v) {
        Connection<V> edge = neighbors.remove(v);
        if (edge == null) return null;
        // loop backwards to make Graph#removeNode faster
        for (int j = outEdges.size()-1; j >= 0; j--) {
            Connection<V> connection = outEdges.get(j);
            if (connection.equals(edge)) {
                outEdges.removeAt(j);
                break;
            }
        }
        if(v.inEdges != null) {
            for (int j = v.inEdges.size() - 1; j >= 0; j--) {
                if (v.inEdges.get(j).equals(edge)) {
                    v.inEdges.removeAt(j);
                    break;
                }
            }
        }
        return edge;
    }

    protected void disconnect() {
        neighbors.clear();
        outEdges.clear();
        if(inEdges != null) inEdges.clear();
    }

    //================================================================================
    // Public Methods
    //================================================================================

    public ObjectList<Connection<V>> getConnections() {
        return outEdges;
    }

    public V getObject() {
        return object;
    }

    //================================================================================
    // Algorithm fields and methods
    //================================================================================

    /**
     * Internal; tracking bit for whether this Node has already been visited during the current algorithm.
     */
    protected boolean visited;
    /**
     * Internal; tracking bit for whether this Node has been checked during the current algorithm.
     */
    protected boolean seen;
    /**
     * Internal; confirmed distance so far to get to this Node from the start.
     */
    protected float distance;
    /**
     * Internal; estimated distance to get from this Node to the goal.
     */
    protected float estimate;
    /**
     * Internal; a reference to the previous Node in a BinaryHeap.
     */
    protected Node<V> prev;
    /**
     * Internal; a utility field used to store depth in some algorithms.
     */
    protected int i;
    /**
     * Internal; a utility field used to distinguish which algorithm last used this Node.
     */
    protected int lastRunID;

    /**
     * If {@code runID} is not equal to {@link #lastRunID}, this resets the internal fields {@link #visited},
     * {@link #seen}, {@link #distance}, {@link #estimate}, {@link #prev}, and {@link #i}, then sets {@link #lastRunID}
     * to {@code runID}.
     * @param runID an int that identifies which run of an algorithm is currently active
     * @return true if anything was reset, or false if {@code runID} is equal to {@link #lastRunID}
     */
    protected boolean resetAlgorithmAttributes(int runID) {
        if (runID == this.lastRunID) return false;
        visited = false;
        prev = null;
        distance = Float.MAX_VALUE;
        estimate = 0;
        i = 0;
        seen = false;
        this.lastRunID = runID;
        return true;
    }
    
    //================================================================================
    // Misc
    //================================================================================


    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return idHash;
    }

    @Override
    public String toString() {
        return "["+object+"]";
    }
}
