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

package com.github.yellowstonegames.freeze.path;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.path.Graph;
import com.github.yellowstonegames.path.UndirectedGraph;
import com.github.yellowstonegames.path.DirectedGraph;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PathTest {
    public static Graph<Vector2> makeGridGraph(Graph<Vector2> graph, int sideLength) {

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                Vector2 v = new Vector2(i, j);
                graph.addVertex(v);
            }
        }

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                if (i<sideLength-1) {
                    Vector2 v1 = new Vector2(i, j), v2 = new Vector2(i+1,j);
                    float dst = v1.dst(v2);
                    graph.addEdge(v1, v2, dst);
                    if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                }
                if (j<sideLength-1) {
                    Vector2 v1 = new Vector2(i, j), v2 = new Vector2(i,j+1);
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
        Kryo kryo = new Kryo();
        kryo.register(UndirectedGraph.class, new UndirectedGraphSerializer());
        kryo.register(Vector2.class);

        int n = 5;
        Graph<Vector2> data = makeGridGraph(new UndirectedGraph<>(), n);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        System.out.println("Undirected byte length: " + bytes.length);
        try (Input input = new Input(bytes)) {
            UndirectedGraph<?> data2 = kryo.readObject(input, UndirectedGraph.class);
//            Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
            Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
            Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
            Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                    data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
//            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testDirectedGraph() {
        Kryo kryo = new Kryo();
        kryo.register(DirectedGraph.class, new DirectedGraphSerializer());
        kryo.register(Vector2.class);

        int n = 5;
        Graph<Vector2> data = makeGridGraph(new DirectedGraph<>(), n);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        System.out.println("Directed byte length: " + bytes.length);
        try (Input input = new Input(bytes)) {
            DirectedGraph<?> data2 = kryo.readObject(input, DirectedGraph.class);
//            Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
            Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
            Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
            Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                    data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
//            Assert.assertEquals(data, data2);
        }
    }

}