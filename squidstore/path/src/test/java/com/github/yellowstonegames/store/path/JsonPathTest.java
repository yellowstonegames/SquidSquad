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

package com.github.yellowstonegames.store.path;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.path.DirectedGraph;
import com.github.yellowstonegames.path.UndirectedGraph;
import com.github.yellowstonegames.store.grid.JsonGrid;
import org.junit.Assert;
import org.junit.Test;

import static com.github.yellowstonegames.grid.Coord.get;

public class JsonPathTest {
    @Test
    public void testDirectedGraph () {
        Json json = new Json(JsonWriter.OutputType.minimal);
		JsonGrid.registerCoord(json);
        JsonPath.registerDirectedGraph(json);
        Coord[] pts = {get(0, 0), get(1, 0), get(0, 1), get(1, 1)};
        DirectedGraph<Coord> graph = new DirectedGraph<>(ObjectList.with(pts));
        graph.addEdge(pts[0], pts[1], 1);
        graph.addEdge(pts[1], pts[0], 2);
        graph.addEdge(pts[1], pts[2], 1);
        graph.addEdge(pts[2], pts[1], 2);
        graph.addEdge(pts[2], pts[3], 1);
        graph.addEdge(pts[3], pts[2], 2);
        String data = json.toJson(graph);
        System.out.println(data);
        DirectedGraph<?> graph2 = json.fromJson(DirectedGraph.class, data);
        Assert.assertEquals(graph, graph2);
        System.out.println();
    }

    @Test
    public void testUndirectedGraph () {
        Json json = new Json(JsonWriter.OutputType.minimal);
		JsonGrid.registerCoord(json);
        JsonPath.registerUndirectedGraph(json);
        Coord[] pts = {get(0, 0), get(1, 0), get(0, 1), get(1, 1)};
        UndirectedGraph<Coord> graph = new UndirectedGraph<>(ObjectList.with(pts));
        graph.addEdge(pts[0], pts[1], 1);
        graph.addEdge(pts[1], pts[2], 1);
        graph.addEdge(pts[2], pts[3], 1);
        String data = json.toJson(graph);
        System.out.println(data);
        UndirectedGraph<?> graph2 = json.fromJson(UndirectedGraph.class, data);
        Assert.assertEquals(graph, graph2);
        System.out.println();
    }
}
