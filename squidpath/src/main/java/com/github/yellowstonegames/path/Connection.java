/*
 * Copyright (c) 2020-2022 See AUTHORS file.
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

import com.github.yellowstonegames.grid.Coord;

/**
 * The most-commonly-used class that represents an edge between nodes on a Graph. Different kinds of graph will use
 * different subclasses of this to represent their edges: {@link DirectedConnection} or {@link UndirectedConnection}.
 * These subclasses don't add new functionality, but they compare differently during hashing and equality checks.
 * @param <V> the vertex type; often {@link Coord}
 * @author earlygrey
 */
public class Connection<V> extends Edge<V> {
    //================================================================================
    // Fields and constants
    //================================================================================

    protected static final float DEFAULT_WEIGHT = 1;

    protected Node<V> a, b;
    protected float weight = DEFAULT_WEIGHT;

    //================================================================================
    // Constructor
    //================================================================================
    
    public Connection() {
    }
    
    public Connection(Node<V> a, Node<V> b) {
        this.a = a;
        this.b = b;
    }
    
    public Connection(Node<V> a, Node<V> b, float weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }

    @Override
    protected void set(Node<V> a, Node<V> b, float weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }

    //================================================================================
    // Internal methods
    //================================================================================

    @Override
    protected Node<V> getInternalNodeA() {
        return a;
    }

    @Override
    protected Node<V> getInternalNodeB() {
        return b;
    }

    //================================================================================
    // Public methods
    //================================================================================

    @Override
    public V getA() {
        return a.object;
    }

    @Override
    public V getB() {
        return b.object;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public void setWeight(float weight) {
        this.weight = weight;
    }

    public Node<V> getNodeA() {
        return a;
    }

    public Node<V> getNodeB() {
        return b;
    }

    //================================================================================
    // Subclasses
    //================================================================================

    /**
     * A Connection that treats A-to-B as a different edge from B-to-A.
     * @param <V> the vertex type; often {@link Coord}
     */
    public static class DirectedConnection<V> extends Connection<V> {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Connection edge = (Connection) o;
            // this assumes a and b are non-null when equals() is called.
            return a.equals(edge.a) && b.equals(edge.b);
        }

        @Override
        public int hashCode() {
            return a.hashCode() * 107 + b.hashCode();
        }

        @Override
        public String toString() {
            return "{" + a + " -> " + b +'}';
        }

    }
    /**
     * A Connection that treats A-to-B and B-to-A as the same edge.
     * @param <V> the vertex type; often {@link Coord}
     */
    public static class UndirectedConnection<V> extends Connection<V> {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Connection edge = (Connection) o;
            // this assumes a and b are non-null when equals() is called.
            return (a.equals(edge.a) && b.equals(edge.b))
                    || (a.equals(edge.b) && b.equals(edge.a));
        }

        @Override
        public int hashCode() {
            return a.hashCode() + b.hashCode();
        }

        @Override
        public String toString() {
            return "{" + a + " <> " + b +'}';
        }
    }

}
