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

package com.github.yellowstonegames.store.path;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.path.*;
import com.github.yellowstonegames.store.grid.JsonGrid;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class JsonPath {
    private JsonPath() {
    }

    /**
     * Registers SquidPath's classes with the given Json object, allowing it to read and write SquidPath types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidPath types.
     */
    public static void registerAll(@NonNull Json json) {
        registerDirectedGraph(json);
        registerUndirectedGraph(json);
        registerCostlyGraph(json);
        registerDefaultGraph(json);
    }
    
    /**
     * Registers DirectedGraph with the given Json object, so DirectedGraph can be written to and read from JSON.
     * You must register the vertex type yourself before you try to serialize or deserialize JSON containing a DirectedGraph.
	 *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDirectedGraph(@NonNull Json json) {
        json.addClassTag("DirG", DirectedGraph.class);
        json.setSerializer(DirectedGraph.class, new Json.Serializer<DirectedGraph>() {
            @Override
            public void write(Json json, DirectedGraph data, Class knownType) {
                json.writeObjectStart(DirectedGraph.class, knownType);
                Set<?> vertices = data.getVertices();
                json.writeArrayStart("v");
                for(Object vertex : vertices) {
                    json.writeValue(vertex, null);
                }
                json.writeArrayEnd();
                ObjectOrderedSet<? extends Connection<?>> edges = data.getEdges();
                json.writeArrayStart("e");
                for(Connection<?> edge : edges) {
                    json.writeValue(edge.getA(), null);
                    json.writeValue(edge.getB(), null);
                    json.writeValue(edge.getWeight(), float.class);
                }
                json.writeArrayEnd();
                json.writeObjectEnd();
            }

            @Override
            public DirectedGraph read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                DirectedGraph<?> graph = new DirectedGraph<>();
                JsonValue entry = jsonData.getChild("v");
                for (; entry != null; entry = entry.next) {
                    graph.addVertex(json.readValue(null, entry));
                }
                entry = jsonData.getChild("e");
                for (; entry != null; entry = entry.next) {
                    ((DirectedGraph) graph).addEdge(json.readValue(null, entry), json.readValue(null, entry = entry.next), (entry = entry.next).asFloat());
                }
                return graph;
            }
        });
    }
    
    /**
     * Registers UndirectedGraph with the given Json object, so UndirectedGraph can be written to and read from JSON.
     * You must register the vertex type yourself before you try to serialize or deserialize JSON containing an UndirectedGraph.
	 *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerUndirectedGraph(@NonNull Json json) {
        json.addClassTag("UdrG", UndirectedGraph.class);
        json.setSerializer(UndirectedGraph.class, new Json.Serializer<UndirectedGraph>() {
            @Override
            public void write(Json json, UndirectedGraph data, Class knownType) {
                json.writeObjectStart(UndirectedGraph.class, knownType);
                Set<?> vertices = data.getVertices();
                json.writeArrayStart("v");
                for(Object vertex : vertices) {
                    json.writeValue(vertex, null);
                }
                json.writeArrayEnd();
                ObjectOrderedSet<? extends Connection<?>> edges = data.getEdges();
                json.writeArrayStart("e");
                for(Connection<?> edge : edges) {
                    json.writeValue(edge.getA(), null);
                    json.writeValue(edge.getB(), null);
                    json.writeValue(edge.getWeight(), float.class);
                }
                json.writeArrayEnd();
                json.writeObjectEnd();
            }

            @Override
            public UndirectedGraph read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                UndirectedGraph<?> graph = new UndirectedGraph<>();
                JsonValue entry = jsonData.getChild("v");
                for (; entry != null; entry = entry.next) {
                    graph.addVertex(json.readValue(null, entry));
                }
                entry = jsonData.getChild("e");
                for (; entry != null; entry = entry.next) {
                    ((UndirectedGraph) graph).addEdge(json.readValue(null, entry), json.readValue(null, entry = entry.next), (entry = entry.next).asFloat());
                }
                return graph;
            }
        });
    }

    /**
     * Registers CostlyGraph with the given Json object, so CostlyGraph can be written to and read from JSON.
     * This registers {@link Coord} using {@link JsonGrid#registerCoord(Json)}.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCostlyGraph(@NonNull Json json) {
        json.addClassTag("CosG", CostlyGraph.class);
        JsonGrid.registerCoord(json);
        json.setSerializer(CostlyGraph.class, new Json.Serializer<CostlyGraph>() {
            @Override
            public void write(Json json, CostlyGraph data, Class knownType) {
                json.writeObjectStart(CostlyGraph.class, knownType);
                json.writeValue("w", data.width);
                json.writeValue("h", data.height);
                Set<Coord> vertices = data.getVertices();
                json.writeArrayStart("v");
                for(Object vertex : vertices) {
                    json.writeValue(vertex);
                }
                json.writeArrayEnd();
                ObjectOrderedSet<? extends Connection<Coord>> edges = data.getEdges();
                json.writeArrayStart("e");
                for(Connection<Coord> edge : edges) {
                    json.writeValue(edge.getA());
                    json.writeValue(edge.getB());
                    json.writeValue(edge.getWeight(), float.class);
                }
                json.writeArrayEnd();
                json.writeObjectEnd();
            }

            @Override
            public CostlyGraph read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CostlyGraph graph = new CostlyGraph();
                graph.width = jsonData.get("w").asInt();
                graph.height = jsonData.get("h").asInt();
                JsonValue entry = jsonData.getChild("v");
                for (; entry != null; entry = entry.next) {
                    graph.addVertex(json.readValue(Coord.class, entry));
                }
                entry = jsonData.getChild("e");
                for (; entry != null; entry = entry.next) {
                    graph.addEdge(json.readValue(Coord.class, entry), json.readValue(Coord.class, entry = entry.next), (entry = entry.next).asFloat());
                }
                return graph;
            }
        });
    }

    /**
     * Registers DefaultGraph with the given Json object, so DefaultGraph can be written to and read from JSON.
     * This registers {@link Coord} using {@link JsonGrid#registerCoord(Json)}.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDefaultGraph(@NonNull Json json) {
        json.addClassTag("DftG", DefaultGraph.class);
        JsonGrid.registerCoord(json);
        json.setSerializer(DefaultGraph.class, new Json.Serializer<DefaultGraph>() {
            @Override
            public void write(Json json, DefaultGraph data, Class knownType) {
                json.writeObjectStart(DefaultGraph.class, knownType);
                json.writeValue("w", data.width);
                json.writeValue("h", data.height);
                Set<Coord> vertices = data.getVertices();
                json.writeArrayStart("v");
                for(Coord vertex : vertices) {
                    json.writeValue(vertex);
                }
                json.writeArrayEnd();
                ObjectOrderedSet<? extends Connection<Coord>> edges = data.getEdges();
                json.writeArrayStart("e");
                for(Connection<Coord> edge : edges) {
                    json.writeValue(edge.getA());
                    json.writeValue(edge.getB());
                    json.writeValue(edge.getWeight(), float.class);
                }
                json.writeArrayEnd();
                json.writeObjectEnd();
            }

            @Override
            public DefaultGraph read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                DefaultGraph graph = new DefaultGraph();
                graph.width = jsonData.get("w").asInt();
                graph.height = jsonData.get("h").asInt();
                JsonValue entry = jsonData.getChild("v");
                for (; entry != null; entry = entry.next) {
                    graph.addVertex(json.readValue(Coord.class, entry));
                }
                entry = jsonData.getChild("e");
                for (; entry != null; entry = entry.next) {
                    graph.addEdge(json.readValue(Coord.class, entry), json.readValue(Coord.class, entry = entry.next), (entry = entry.next).asFloat());
                }
                return graph;
            }
        });
    }
}
