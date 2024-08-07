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
package com.github.yellowstonegames.path.sg.algorithms;

import com.github.yellowstonegames.path.sg.DirectedGraph;
import com.github.yellowstonegames.path.sg.Graph;
import com.github.yellowstonegames.path.sg.algorithms.Algorithms;

public class DirectedGraphAlgorithms<V> extends Algorithms<V> {

    public DirectedGraphAlgorithms(DirectedGraph<V> graph) {
        super(graph);
    }

    /**
     * Sort the vertices of this graph in topological order. That is, for every edge from vertex u to vertex v, u comes before v in the ordering.
     * This is reflected in the iteration order of the collection returned by {@link Graph#getVertices()}.
     * Note that the graph cannot contain any cycles for a topological order to exist. If a cycle exists, this method will do nothing.
     * @return true if the sort was successful, false if the graph contains a cycle
     */
    /*public boolean topologicalSort() {
        return implementations.topologicalSort();
    }*/

    /**
     * Perform a topological sort on the graph, and puts the sorted vertices in the supplied list.
     * That is, for every edge from vertex u to vertex v, u will come before v in the supplied list.
     * Note that the graph cannot contain any cycles for a topological order to exist. If a cycle exists, the sorting procedure will
     * terminate and the supplied list will only contain the vertices up until the point of termination.
     * @return true if the sort was successful, false if the graph contains a cycle
     */
   /* public boolean topologicalSort(List<V> sortedVertices) {
        return implementations.topologicalSort(sortedVertices);
    }*/


}
