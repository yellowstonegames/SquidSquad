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

import com.github.yellowstonegames.path.sg.UndirectedGraph;
import com.github.yellowstonegames.path.sg.algorithms.Algorithms;
import com.github.yellowstonegames.path.sg.algorithms.MinimumWeightSpanningTree;

public class UndirectedGraphAlgorithms<V> extends Algorithms<V> {

    public UndirectedGraphAlgorithms(UndirectedGraph<V> graph) {
        super(graph);
    }

    /**
     * Find a minimum weight spanning tree using Kruskal's algorithm.
     * @return a Graph object containing a minimum weight spanning tree (if this graph is connected -
     * in general a minimum weight spanning forest)
     */
    public UndirectedGraph<V> findMinimumWeightSpanningTree() {
        MinimumWeightSpanningTree<V> algorithm = new MinimumWeightSpanningTree<>(requestRunID(), (UndirectedGraph<V>) graph, true);
        algorithm.finish();
        return algorithm.getSpanningTree();
    }


}
