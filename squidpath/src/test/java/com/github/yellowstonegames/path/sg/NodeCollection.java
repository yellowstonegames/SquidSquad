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

class NodeCollection<V> extends AbstractCollection<Node<V>> {

    NodeMap<V> nodeMap;

    NodeCollection(NodeMap<V> nodeMap) {
        this.nodeMap = nodeMap;
    }

    @Override
    public int size() {
        return nodeMap.size;
    }

    @Override
    public boolean contains(Object o) {
        return nodeMap.contains((V) o);
    }

    @Override
    public Iterator<Node<V>> iterator() {
        return new NodeIterator<>(nodeMap);
    }


    @Override
    public Object[] toArray() {
        Object[] array = new Object[nodeMap.size];
        int index = 0;
        for (int i = 0; i < nodeMap.table.length; i++) {
            Node<V> node = nodeMap.table[i];
            while (node != null) {
                array[index++] = node.object;
                node = node.nextInBucket;
            }
        }
        return array;
    }


    @Override
    public <T> T[] toArray(T[] array) {
        if (array.length < nodeMap.size) {
            array = Arrays.copyOf(array, nodeMap.size);
        }
        int index = 0;
        for (int i = 0; i < nodeMap.table.length; i++) {
            Node<V> node = nodeMap.table[i];
            while (node != null) {
                array[index++] = (T) node.object;
                node = node.nextInBucket;
            }
        }
        return array;
    }


    @Override
    public boolean add(Node<V> v) {
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
        for (Object e : collection) {
            if (!contains(e)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Node<V>> collection) {
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
        NodeCollection<?> that = (NodeCollection<?>) o;
        return nodeMap.equals(that.nodeMap);
    }

    @Override
    public int hashCode() {
        return nodeMap.hashCode();
    }

}
