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

import com.github.yellowstonegames.path.sg.TestUtils.Vector2;
import com.github.yellowstonegames.path.sg.utils.Heuristic;
import com.github.yellowstonegames.path.sg.utils.SearchProcessor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlgorithmsTest {

    @Test
    public void shortestPathShouldBeCorrect() {
        int n = 5;
        Graph<Vector2> undirectedGraph = TestUtils.makeGridGraph(new UndirectedGraph<>(), n);
        Graph<Vector2> diGraph = TestUtils.makeGridGraph(new DirectedGraph<>(), n);

        Vector2 start = new Vector2(0, 0), end = new Vector2(n - 1, n - 1);
        Path<Vector2> path;

        // without heuristic
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));

        // with heuristic
        Heuristic<Vector2> h = Vector2::dst;

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));
        assertTrue("Shortest path is not connected", diGraph.algorithms().isConnected(start, end));

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(2*(n-1) + 1, path.size());
        assertEquals(start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));
        assertTrue("Shortest path is not connected", undirectedGraph.algorithms().isConnected(start, end));


        // no path exists
        undirectedGraph.disconnect(end);
        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertTrue(!undirectedGraph.algorithms().isConnected(start, end));

        diGraph.disconnect(end);
        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertTrue(!diGraph.algorithms().isConnected(start, end));
    }


    private static boolean pathIsConnected(Path<Vector2> path, Graph<Vector2> graph) {
        for (int i = 0; i < path.size()-1; i++) {
            Vector2 current = path.get(i);
            Vector2 next = path.get(i+1);
            if (!graph.edgeExists(current, next)) {
                System.out.println("Problem at index " + i);
                System.out.println(current + " does not connect to " + next);
                next = path.get(i+1);
                return false;
            }
        }
        return true;
    }

    @Test
    public void cyclesShouldBeDetected() {

        Graph<Integer> graph = new DirectedGraph<>();

        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        assertTrue(!graph.algorithms().containsCycle());

        graph.addEdge(0,2);
        assertTrue(!graph.algorithms().containsCycle());

        graph.addEdge(2,0);
        assertTrue(graph.algorithms().containsCycle());

        graph = new UndirectedGraph<>();

        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        assertTrue(!graph.algorithms().containsCycle());

        graph.addEdge(0,2);
        assertTrue(graph.algorithms().containsCycle());

    }
    
    private DirectedGraph<Integer> createDirectedSearchGraph() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();

        for (int i = 0; i < 7; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 4);
        graph.addEdge(3, 5);
        graph.addEdge(4, 5);
        
        return graph;
    }

    @Test
    public void bfsShouldWork() {
        Graph<Integer> graph = createDirectedSearchGraph();

        Graph<Integer> tree = graph.createNew();

        SearchProcessor<Integer> processor = (step) -> {
            tree.addVertex(step.vertex());
            if (step.count() > 0) {
                tree.addEdge(step.edge().getA(), step.edge().getB());
            }
        };

        tree.addVertex(0);
        graph.algorithms().breadthFirstSearch(0, processor);
        assertEquals(6, tree.size());
        assertEquals(5, tree.getEdgeCount());

        List<Integer> vxs = new ArrayList<>(tree.getVertices());

        List<List<Integer>> expectedOrders = new ArrayList<>();
        expectedOrders.add(Arrays.asList(0, 1, 2, 3, 4, 5));
        expectedOrders.add(Arrays.asList(0, 2, 1, 3, 4, 5));
        expectedOrders.add(Arrays.asList(0, 1, 2, 4, 3, 5));
        expectedOrders.add(Arrays.asList(0, 2, 1, 3, 4, 5));

        assertTrue("BFS not in correct order", expectedOrders.contains(vxs));
    }

    @Test
    public void dfsShouldWork() {
        Graph<Integer> graph = createDirectedSearchGraph();
        Graph<Integer> tree = graph.createNew();

        SearchProcessor<Integer> processor = step -> {
            if (step.depth() > 4) {
                step.ignore();
                return;
            }
            tree.addVertex(step.vertex());
            if (step.count() > 0) {
                tree.addEdge(step.edge().getA(), step.edge().getB());
            }
        };

        graph.algorithms().depthFirstSearch(0, processor);


        assertEquals(6, tree.size());
        assertEquals(5, tree.getEdgeCount());
        List<Integer> vxs = new ArrayList<>(tree.getVertices());

        List<List<Integer>> expectedOrders = new ArrayList<>();
        expectedOrders.add(Arrays.asList(0, 1, 3, 5, 2, 4));
        expectedOrders.add(Arrays.asList(0, 2, 4, 5, 1, 3));
        assertTrue("DFS not in correct order", expectedOrders.contains(vxs));
    }

    @Test
    public void topologicalSortShouldWork() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        int n = 10;
        for (int i = 0; i < n; i++) graph.addVertex(i);

        graph.addEdge(9,8);
        graph.addEdge(6,7);
        assertTrue(graph.topologicalSort());

        graph.removeAllEdges();

        for (int i = 0; i < n-1; i++) graph.addEdge(i+1, i);

        assertTrue(graph.topologicalSort());
        int i = n-1;
        for (Integer vertex : graph.getVertices()) {
            Integer expected = Integer.valueOf(i--);
            assertEquals(expected, vertex);
        }


        graph.addEdge(n/2, n/2 + 1);
        boolean success = graph.topologicalSort();
        assertTrue(!success);

        graph = new DirectedGraph<>();
        graph.addVertices(0, 1, 2, 3, 4, 5);
        graph.addEdge(2,0);
        graph.addEdge(1,2);
        graph.addEdge(4,1);
        graph.addEdge(4,2);
        graph.addEdge(3,5);
        assertTrue(graph.topologicalSort());

        graph = new DirectedGraph<>();
        graph.addVertices(0, 1, 2, 3, 4, 5);
        graph.addEdge(2,0);
        graph.addEdge(1,2);
        graph.addEdge(4,1);
        graph.addEdge(4,2);
        graph.addEdge(3,5);

        graph.addEdge(2,4);
        assertTrue(!graph.topologicalSort());
    }

    @Test
    public void mwstShouldBeTree() {

        int n = 20;
        UndirectedGraph<Integer> graph = new UndirectedGraph<>();
        for (int i = 0; i < n; i++) graph.addVertex(i);
        GraphBuilder.buildCompleteGraph(graph);

        graph.getEdge(0, 1).setWeight(4);
        graph.getEdge(4, 6).setWeight(0.5f);

        Graph<Integer> mwst = graph.algorithms().findMinimumWeightSpanningTree();

        assertEquals("Tree doesn't have correct number of vertices", n, mwst.size());
        assertEquals("Tree doesn't have correct number of edges", n-1, mwst.getEdgeCount());
        assertTrue("Tree contains a cycle", !mwst.algorithms().containsCycle());
        assertEquals("Tree is not minimum weight", n-1 - 0.5f, mwst.getEdges().stream().mapToDouble(e -> e.getWeight()).sum(), 0.0001f);

    }
}
