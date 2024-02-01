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

import com.github.yellowstonegames.path.sg.Edge;
import com.github.yellowstonegames.path.sg.Node;
import com.github.yellowstonegames.path.sg.Path;
import com.github.yellowstonegames.path.sg.algorithms.AlgorithmPath;

/**
 * An object representing a "step" in a search algorithm. Usually this corresponds to visiting or processing a vertex.
 * @param <V>
 */

public class SearchStep<V> {

    boolean terminate, ignore;
    Node<V> node;
    int count = -1;

    SearchStep() {

    }

    void prepare(Node<V> node) {
        this.node = node;
        terminate = false;
        ignore = false;
        count++;
    }

    /**
     * Immediately terminate the search.
     */
    public void terminate() {
        terminate = true;
    }

    /**
     * Ignore the current vertex, and do not check its neighbours in this step.
     */
    public void ignore() {
        ignore = true;
    }

    /**
     * @return the vertex being currently processed.
     */
    public V vertex() {
        return node.getObject();
    }

    /**
     * @return the edge from which the current vertex was found. {@link Edge#getB()} is equal to the current vertex.
     */
    public Edge<V> edge() {
        return node.getConnection();
    }

    /**
     * @return the vertex from which the current vertex was found. Equal to {@link Edge#getA()} on the edge returned by {@link #edge()}.
     *
     */
    public V previous() {
        return node.getConnection().getA();
    }

    /**
     * @return the number of vertices traversed in order to find the current vertex, not including the initial vertex.
     */
    public int depth() {
        return node.getIndex();
    }

    /**
     * @return the sum of edge weights on a path from the initial vertex to the current, along the search path taken.
     * For a shortest path search this represents the actual minimum distance of all shortest paths from the initial vertex to the current,
     * for a breadth or depth first search this is not necessarily true.
     */
    public float distance() {
        return node.getDistance();
    }

    /**
     * @return the number of processing steps so far.
     */
    public int count() {
        return count;
    }

    /**
     * Reconstruct the path from the initial vertex to the current vertex that the search algorithm took.
     * For a shortest path search this is an actual shortest path from the initial vertex to the current in the graph,
     * for a breadth or depth first search this is not necessarily true.
     * @return a path from the initial vertex to the current
     */
    public Path<V> createPath() {
        return new AlgorithmPath<>(node);
    }

}
