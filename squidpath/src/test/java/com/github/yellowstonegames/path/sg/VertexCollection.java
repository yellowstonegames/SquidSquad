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

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.github.yellowstonegames.path.sg.Errors;
import com.github.yellowstonegames.path.sg.Node;
import com.github.yellowstonegames.path.sg.NodeMap;
import com.github.yellowstonegames.path.sg.NodeMap.NodeIterator;

class VertexCollection<V> extends AbstractCollection<V> {

    final NodeMap<V> nodeMap;

    VertexCollection(NodeMap<V> nodeMap) {
        this.nodeMap = nodeMap;
    }

    @Override
    public int size() {
        return nodeMap.size;
    }

    @Override
    public boolean isEmpty() {
        return nodeMap.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return o != null && nodeMap.contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return new VertexIterator<>(nodeMap);
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[nodeMap.size];
        int index = 0;
        Node<V> node = nodeMap.head;
        while (node != null) {
            array[index++] = node.object;
            node = node.nextInOrder;
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        if (array.length < nodeMap.size) {
            array = Arrays.copyOf(array, nodeMap.size);
        }
        int index = 0;
        Node<V> node = nodeMap.head;
        while (node != null) {
            array[index++] = (T) node.object;
            node = node.nextInOrder;
        }
        return array;
    }

    @Override
    public boolean add(V v) {
        Errors.throwModificationException();
        return false;
    }

    @Override
    public boolean remove(Object o) {
        Errors.throwModificationException();
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends V> collection) {
        Errors.throwModificationException();
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        Errors.throwModificationException();
        return false;
    }


    @Override
    public boolean retainAll(Collection<?> collection) {
        Errors.throwModificationException();
        return false;
    }

    @Override
    public void clear() {
        Errors.throwModificationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertexCollection<?> that = (VertexCollection<?>) o;
        return nodeMap.equals(that.nodeMap);
    }

    @Override
    public int hashCode() {
        return nodeMap.hashCode();
    }

    static class VertexIterator<V> implements Iterator<V> {

        private final NodeIterator<V> nodeIterator;

        VertexIterator(NodeMap<V> nodeMap) {
            nodeIterator = new NodeIterator<>(nodeMap);
        }

        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext();
        }

        @Override
        public V next() {
            return nodeIterator.next().object;
        }

        @Override
        public void remove() {
            Errors.throwModificationException();
        }
    }

}
