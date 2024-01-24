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
import com.github.yellowstonegames.path.Connection;
import com.github.yellowstonegames.path.Edge;
import com.github.yellowstonegames.path.UndirectedGraph;

import java.util.Collection;
import java.util.Set;

/**
 * Kryo {@link Serializer} for simple-graphs {@link UndirectedGraph}s.
 * You should register the vertex type when you register this as the serializer for UndirectedGraph.
 */
public class UndirectedGraphSerializer extends Serializer<UndirectedGraph<?>> {

    public UndirectedGraphSerializer() {
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final UndirectedGraph<?> data) {
        Collection<?> vertices = data.getVertices();
        Set<? extends Edge<?>> edges = data.getEdges();
        int length = vertices.size();
        output.writeInt(length, true);
        for(Object v : vertices) {
            kryo.writeClassAndObject(output, v);
        }
        length = edges.size();
        output.writeInt(length, true);
        for(Edge<?> e : edges) {
            kryo.writeClassAndObject(output, e.getA());
            kryo.writeClassAndObject(output, e.getB());
//            kryo.writeClassAndObject(output, e.getWeightFunction());
            output.writeFloat(e.getWeight());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked", "UnnecessaryLocalVariable"})
    @Override
    public UndirectedGraph<?> read(final Kryo kryo, final Input input, final Class<? extends UndirectedGraph<?>> dataClass) {
        UndirectedGraph<?> graph = new UndirectedGraph<>();
        UndirectedGraph raw = graph;
        int length = input.readInt(true);
        for (int i = 0; i < length; i++) {
            raw.addVertex(kryo.readClassAndObject(input));
        }
        length = input.readInt(true);
        for (int i = 0; i < length; i++) {
//            raw.addEdge(kryo.readClassAndObject(input), kryo.readClassAndObject(input), (WeightFunction) kryo.readClassAndObject(input));
            raw.addEdge(kryo.readClassAndObject(input), kryo.readClassAndObject(input), input.readFloat());
        }
        return graph;
    }

    @SuppressWarnings({"rawtypes", "unchecked", "UnnecessaryLocalVariable"})
    @Override
    public UndirectedGraph<?> copy(Kryo kryo, UndirectedGraph<?> original) {
        UndirectedGraph<?> graph = new UndirectedGraph<>();
        UndirectedGraph raw = graph;
        Collection<?> vertices = graph.getVertices();
        for(Object v : vertices){
            raw.addVertex(kryo.copy(v));
        }
        Collection<? extends Edge<?>> edges = graph.getEdges();
        for(Edge<?> e : edges){
//            raw.addEdge(kryo.copy(e.getA()), kryo.copy(e.getB()), e.getWeightFunction());
            raw.addEdge(kryo.copy(e.getA()), kryo.copy(e.getB()), e.getWeight());
        }
        return graph;
    }
}