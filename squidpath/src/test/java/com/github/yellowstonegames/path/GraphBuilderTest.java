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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GraphBuilderTest {

    /*static class GridPoint {
        final int i, j;
        GridPoint(int i, int j) {
            this.i = i;
            this.j = j;
        }
        float dst (GridPoint v) {
            int x_d = v.i - i, y_d = v.j - j;
            return (float) Math.sqrt(x_d * x_d + y_d * y_d);
        }
    }

    @Test
    public void testExample() {
        int n = 10;
        UndirectedGraph<GridPoint> graph = new UndirectedGraph<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                graph.addVertex(new GridPoint(i, j));
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i<n-1) {
                    GridPoint v1 = new GridPoint(i, j), v2 = new GridPoint(i+1,j);
                    graph.addEdge(v1, v2, v1.dst(v2));
                }
                if (j<n-1) {
                    GridPoint v1 = new GridPoint(i, j), v2 = new GridPoint(i,j+1);
                    graph.addEdge(v1, v2, v1.dst(v2));
                }
            }
        }
    }*/

    @Test
    public void completeGraphCanBeBuilt() {
        int n = 4;
        UndirectedGraph<Integer> graph = new UndirectedGraph<>();
        for (int i = 0; i < n; i++) graph.addVertex(i);

        GraphBuilder.buildCompleteGraph(graph);

        assertEquals(n, graph.size());

        assertEquals(n*(n-1)/2, graph.getEdgeCount());

        DirectedGraph<Integer> digraph = new DirectedGraph<>();
        for (int i = 0; i < n; i++) digraph.addVertex(i);

        GraphBuilder.buildCompleteGraph(digraph);

        assertEquals(n, digraph.size());

        assertEquals(n*(n-1), digraph.getEdgeCount());
    }


}
