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

import com.github.yellowstonegames.path.sg.Edge;
import com.github.yellowstonegames.path.sg.Node;
import com.github.yellowstonegames.path.sg.utils.WeightFunction;

public abstract class Connection<V> extends Edge<V> {

    //================================================================================
    // Fields and constants
    //================================================================================

    Node<V> a, b;

    WeightFunction<V> weight;

    //================================================================================
    // Constructor
    //================================================================================

    Connection() {

    }

    //================================================================================
    // Internal methods
    //================================================================================

    @Override
    void set(Node<V> a, Node<V> b, WeightFunction<V> weight) {
        this.a = a;
        this.b = b;
        setWeight(weight);
    }

    @Override
    Node<V> getInternalNodeA() {
        return a;
    }

    @Override
    Node<V> getInternalNodeB() {
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
        return weight.getWeight(getA(), getB());
    }

    @Override
    public void setWeight(float weight) {
        setWeight((a, b) -> weight);
    }

    @Override
    public void setWeight(WeightFunction<V> weight) {
        this.weight = weight;
    }

    @Override
    public WeightFunction<V> getWeightFunction() {
        return weight;
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

    static class DirectedConnection<V> extends Connection<V> {

        @Override
        public boolean hasEndpoints(V u, V v) {
            return getA().equals(u) && getB().equals(v);
        }

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
            return (int) (a.hashCode() * 0xC13FA9A902A6328FL + b.hashCode() * 0x91E10DA5C79E7B1DL >>> 32);
        }

        @Override
        public String toString() {
            return "{" + a + " -> " + b + ", " + getWeight() + "}";
        }

    }

    static class UndirectedConnection<V> extends Connection<V> {

        @Override
        public boolean hasEndpoints(V u, V v) {
            return hasEndpoint(u) && hasEndpoint(v);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Connection edge = (Connection) o;
            return (a.equals(edge.a) && b.equals(edge.b)) || (a.equals(edge.b) && b.equals(edge.a));
        }

        @Override
        public int hashCode() {
            return a.hashCode() ^ b.hashCode();
        }

        @Override
        public String toString() {
            return "{" + a + " <> " + b + ", " + getWeight() +"}";
        }
    }

}
