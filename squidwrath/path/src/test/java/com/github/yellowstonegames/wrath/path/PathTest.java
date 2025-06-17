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

package com.github.yellowstonegames.wrath.path;

import com.github.yellowstonegames.wrath.grid.CoordSerializer;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.path.*;
import org.apache.fory.Fory;
import org.apache.fory.config.Language;
import org.apache.fory.logging.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PathTest {
    public static Graph<Coord> makeGridGraph(Graph<Coord> graph, int sideLength) {

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                Coord v = Coord.get(i, j);
                graph.addVertex(v);
            }
        }

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                if (i<sideLength-1) {
                    Coord v1 = Coord.get(i, j), v2 = Coord.get(i+1,j);
                    float dst = v1.dst(v2);
                    graph.addEdge(v1, v2, dst);
                    if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                }
                if (j<sideLength-1) {
                    Coord v1 = Coord.get(i, j), v2 = Coord.get(i,j+1);
                    float dst = v1.dst(v2);
                    graph.addEdge(v1, v2, dst);
                    if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                }
            }
        }

        return graph;
    }

    @Test
    public void testUndirectedGraph() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.register(UndirectedGraph.class);

        int n = 5;
        Graph<Coord> data = makeGridGraph(new UndirectedGraph<>(), n);

        byte[] bytes = fory.serializeJavaObject(data);
        System.out.println("Undirected byte length: " + bytes.length);
        {
            UndirectedGraph<?> data2 = fory.deserializeJavaObject(bytes, UndirectedGraph.class);
            Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
            Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
            Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                    data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testDirectedGraph() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.register(DirectedGraph.class);

        int n = 5;
        Graph<Coord> data = makeGridGraph(new DirectedGraph<>(), n);

        byte[] bytes = fory.serializeJavaObject(data);
        System.out.println("Directed byte length: " + bytes.length);
        {
            DirectedGraph<?> data2 = fory.deserializeJavaObject(bytes, DirectedGraph.class);
            Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
            Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
            Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                    data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testDefaultGraph() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.register(DefaultGraph.class);

        Graph<Coord> data = new DefaultGraph(new char[][]{
                "######".toCharArray(),
                "#....#".toCharArray(),
                "#....#".toCharArray(),
                "#..#.#".toCharArray(),
                "#....#".toCharArray(),
                "######".toCharArray(),
        }, true);

        byte[] bytes = fory.serializeJavaObject(data);
        System.out.println("Default byte length: " + bytes.length);
        {
            DefaultGraph data2 = fory.deserializeJavaObject(bytes, DefaultGraph.class);
            Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
            Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
            Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                    data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCostlyGraph() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.register(CostlyGraph.class);

        Graph<Coord> data = new CostlyGraph(new char[][]{
                "######".toCharArray(),
                "#....#".toCharArray(),
                "#....#".toCharArray(),
                "#..#.#".toCharArray(),
                "#....#".toCharArray(),
                "######".toCharArray(),
        }, true);

        byte[] bytes = fory.serializeJavaObject(data);
        System.out.println("Costly byte length: " + bytes.length);
        {
            CostlyGraph data2 = fory.deserializeJavaObject(bytes, CostlyGraph.class);
            Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
            Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
            Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                    data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
            Assert.assertEquals(data, data2);
        }
    }
}