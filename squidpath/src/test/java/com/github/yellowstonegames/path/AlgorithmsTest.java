/*
MIT License

Copyright (c) 2020 earlygrey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.github.yellowstonegames.path;

import com.github.tommyettinger.ds.ObjectDeque;
import com.github.yellowstonegames.grid.Coord;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlgorithmsTest {

    @Test
    public void shortestPathShouldBeCorrect() {
        int n = 20;
        Graph<Coord> undirectedGraph = TestUtils.makeGridGraph(new UndirectedGraph<>(), n);
        Graph<Coord> diGraph = TestUtils.makeGridGraph(new DirectedGraph<>(), n);

        Coord start = Coord.get(0, 0), end = Coord.get(n - 1, n - 1);
        ObjectDeque<Coord> path;

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
        Heuristic<Coord> h = Coord::distance;

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
        assertFalse(undirectedGraph.algorithms().isConnected(start, end));

        diGraph.disconnect(end);
        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(diGraph.algorithms().isConnected(start, end));
    }


    private static boolean pathIsConnected(ObjectDeque<Coord> path, Graph<Coord> graph) {
        for (int i = 0; i < path.size()-1; i++) {
            if (!graph.edgeExists(path.get(i), path.get(i+1))) return false;
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
        assertFalse(graph.algorithms().containsCycle());

        graph.addEdge(0,2);
        assertFalse(graph.algorithms().containsCycle());

        graph.addEdge(2,0);
        assertTrue(graph.algorithms().containsCycle());

        graph = new UndirectedGraph<>();

        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        assertFalse(graph.algorithms().containsCycle());

        graph.addEdge(0,2);
        assertTrue(graph.algorithms().containsCycle());

    }
    private Graph<Integer> createSearchGraph() {
        Graph<Integer> graph = new UndirectedGraph<>();

        for (int i = 0; i < 5; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(1, 3);
        graph.addEdge(0, 2);
        graph.addEdge(2, 3);

        return graph;
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
        Graph<Integer> graph = createSearchGraph();

        Graph<Integer> tree = graph.algorithms().breadthFirstSearch(0);
        assertEquals(4, tree.size());
        assertEquals(3, tree.getEdgeCount());

        int count = 0;
        for (Integer vertex : tree.getVertices()) {
            switch(count) {
                case 0:
                    assertEquals(Integer.valueOf(0), vertex);
                    break;
                case 3:
                    assertEquals(Integer.valueOf(3), vertex);
                    break;
            }
            count++;
        }
    }

    @Test
    public void dfsShouldWork() {
        Graph<Integer> graph = createSearchGraph();

        Graph<Integer> tree = graph.algorithms().depthFirstSearch(0);

        assertEquals(4, tree.size());
        assertEquals(3, tree.getEdgeCount());

        int count = 0;
        for (Integer vertex : tree.getVertices()) {
            switch(count) {
                case 0:
                    assertEquals(Integer.valueOf(0), vertex);
                    break;
                case 2:
                    assertEquals(Integer.valueOf(3), vertex);
                    break;
            }
            count++;
        }
    }

    @Test
    public void topologicalSortShouldWork() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        int n = 10;
        for (int i = 0; i < n; i++) graph.addVertex(i);

        graph.addEdge(9,8);
        graph.addEdge(6,7);
        assertTrue(graph.algorithms().topologicalSort());

        graph.removeAllEdges();

        for (int i = 0; i < n-1; i++) graph.addEdge(i+1, i);

        assertTrue(graph.algorithms().topologicalSort());
        int i = n-1;
        for (Integer vertex : graph.getVertices()) {
            Integer expected = Integer.valueOf(i--);
            assertEquals(expected, vertex);
        }


        graph.addEdge(n/2, n/2 + 1);
        boolean success = graph.algorithms().topologicalSort();
        assertTrue(!success);

        graph = new DirectedGraph<>();
        graph.addVertices(0, 1, 2, 3, 4, 5);
        graph.addEdge(2,0);
        graph.addEdge(1,2);
        graph.addEdge(4,1);
        graph.addEdge(4,2);
        graph.addEdge(3,5);
        assertTrue(graph.algorithms().topologicalSort());

        graph = new DirectedGraph<>();
        graph.addVertices(0, 1, 2, 3, 4, 5);
        graph.addEdge(2,0);
        graph.addEdge(1,2);
        graph.addEdge(4,1);
        graph.addEdge(4,2);
        graph.addEdge(3,5);

        graph.addEdge(2,4);
        assertTrue(!graph.algorithms().topologicalSort());
    }

    @Test
    public void mwstShouldBeTree() {

        int n = 4;
        UndirectedGraph<Integer> graph = new UndirectedGraph<>();
        for (int i = 0; i < n; i++) graph.addVertex(i);
        GraphBuilder.buildCompleteGraph(graph);

        Graph<Integer> mwst = graph.algorithms().findMinimumWeightSpanningTree();

        assertEquals(n, mwst.size());
        assertEquals(n-1, mwst.getEdgeCount());

        assertTrue(!mwst.algorithms().containsCycle());
    }}
