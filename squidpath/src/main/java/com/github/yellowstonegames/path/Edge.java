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
package com.github.yellowstonegames.path;

import com.github.yellowstonegames.grid.Coord;

/**
 * Abstract superclass of any connection between nodes on a graph.
 * @param <V> the vertex type; often {@link Coord}
 * @author earlygrey
 */
public abstract class Edge<V> {
    public Edge(){}

    public abstract V getA();
    public abstract V getB();

    public abstract boolean hasEndpoints(V u, V v);
    public boolean hasEndpoint(V u) {
        return getA().equals(u) || getB().equals(u);
    }

    public abstract float getWeight();
    public abstract void setWeight(float weight);

    protected abstract Node<V> getInternalNodeA();
    protected abstract Node<V> getInternalNodeB();

    protected abstract void set(Node<V> a, Node<V> b, float weight);
}
