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
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.path.Connection;
import com.github.yellowstonegames.path.DirectedGraph;
import com.github.yellowstonegames.store.core.JsonCore;
import com.github.yellowstonegames.store.grid.JsonGrid;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

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
                DirectedGraph raw = graph;
                JsonValue entry = jsonData.getChild("v");
                for (; entry != null; entry = entry.next) {
                    raw.addVertex(json.readValue(null, entry));
                }
                entry = jsonData.getChild("e");
                for (; entry != null; entry = entry.next) {
                    raw.addEdge(json.readValue(null, entry), json.readValue(null, entry = entry.next), (entry = entry.next).asFloat());
                }
                return graph;
            }
        });
    }
}
