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
 *
 */

package com.github.yellowstonegames.freeze.path;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.yellowstonegames.freeze.grid.CoordSerializer;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.path.Connection;
import com.github.yellowstonegames.path.DefaultGraph;

import java.util.Set;

/**
 * Kryo {@link Serializer} for simple-graphs {@link DefaultGraph}s.
 * This needs {@link CoordSerializer} (or some other serializer) registered for {@link Coord}.
 */
public class DefaultGraphSerializer extends Serializer<DefaultGraph> {

    public DefaultGraphSerializer() {
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final DefaultGraph data) {
        output.writeInt(data.width, true);
        output.writeInt(data.height, true);
        Set<Coord> vertices = data.getVertices();
        ObjectOrderedSet<? extends Connection<Coord>> edges = data.getEdges();
        int length = vertices.size();
        output.writeInt(length, true);
        for(Coord v : vertices) {
            kryo.writeObject(output, v);
        }
        length = edges.size();
        output.writeInt(length, true);
        for(Connection<Coord> e : edges) {
            kryo.writeObject(output, e.getA());
            kryo.writeObject(output, e.getB());
            output.writeFloat(e.getWeight());
        }
    }

    @Override
    public DefaultGraph read(final Kryo kryo, final Input input, final Class<? extends DefaultGraph> dataClass) {
        DefaultGraph graph = new DefaultGraph();
        graph.width = input.readInt(true);
        graph.height = input.readInt(true);
        int length = input.readInt(true);
        for (int i = 0; i < length; i++) {
            graph.addVertex(kryo.readObject(input, Coord.class));
        }
        length = input.readInt(true);
        for (int i = 0; i < length; i++) {
            graph.addEdge(kryo.readObject(input, Coord.class), kryo.readObject(input, Coord.class), input.readFloat());
        }
        return graph;
    }

    @Override
    public DefaultGraph copy(Kryo kryo, DefaultGraph original) {
        DefaultGraph graph = new DefaultGraph();
        Set<Coord> vertices = graph.getVertices();
        for(Coord v : vertices){
            graph.addVertex(kryo.copy(v));
        }
        ObjectOrderedSet<? extends Connection<Coord>> edges = graph.getEdges();
        for(Connection<Coord> e : edges){
            graph.addEdge(e.getA(), e.getB(), e.getWeight());
        }
        return graph;
    }
}