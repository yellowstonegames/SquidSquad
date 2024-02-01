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

import java.util.Comparator;
import java.util.Iterator;

/**
 * A hash structure with objects of type V as keys and Node<V> objects as values.
 * Keys assigned to the same bucket are chained in a singly linked list.
 * All the Node<V> objects additionally form a separate doubly linked list to allow a consistent iteration order.
 *
 */
class NodeMap<V> {

    final transient Graph<V> graph;

    // array of "buckets"
    Node<V>[] table;

    // linked list of map entries
    Node<V> head, tail;

    int size = 0;
    int occupiedBuckets = 0;
    static final int MIN_TABLE_LENGTH = 32;
    static final float RESIZE_THRESHOLD = 0.7f;
    int threshold = (int) (RESIZE_THRESHOLD * MIN_TABLE_LENGTH);

    // collections for returning to the user
    transient VertexCollection<V> vertexCollection;
    transient NodeCollection<V> nodeCollection;

    @SuppressWarnings("unchecked")
    public NodeMap(Graph<V> graph) {
        this.graph = graph;
        table = (Node<V>[]) new Node[MIN_TABLE_LENGTH];
        vertexCollection = new VertexCollection<>(this);
        nodeCollection = new NodeCollection<>(this);
    }

    /**
     * Return the `Node<V>` to which the vertex v is mapped, or null if not in the map.
     */
    Node<V> get(Object v) {
        int hash = hash(v);
        int i = getIndex(hash);
        Node<V> bucketHead = table[i];
        if (bucketHead == null) return null;

        Node<V> currentNode = bucketHead;
        while (currentNode != null) {
            if (v.equals(currentNode.object)) return currentNode;
            currentNode = currentNode.nextInBucket;
        }

        return null;
    }

    boolean contains(Object v) {
        return get(v) != null;
    }

    /**
     * Create a Node<V> and associate the vertex v with it.
     * @return the `Node<V>` if v is not in the map, or null if it already is.
     */
    Node<V> put(V v) {
        // checking the size before adding might resize even if v is already in the map,
        // but it will only be off by one
        checkLength();

        int hash = hash(v);
        int i = getIndex(hash);
        Node<V> bucketHead = table[i];
        if (bucketHead == null) {
            // first in bucket
            bucketHead = new Node<>(v, graph.isDirected(), hash);
            bucketHead.mapHash = hash;
            table[i] = bucketHead;
            size++;
            occupiedBuckets++;
            addToList(bucketHead);
            return bucketHead;
        }

        // find last in bucket
        Node<V> currentNode = bucketHead, previousNode = null;
        while (currentNode != null) {
            if (v.equals(currentNode.object)) return null;
            previousNode = currentNode;
            currentNode = currentNode.nextInBucket;
        }

        currentNode = new Node<>(v, graph.isDirected(), hash);
        currentNode.mapHash = hash;
        previousNode.nextInBucket = currentNode;
        size++;
        addToList(currentNode);
        return currentNode;
    }

    /**
     * Add the node to the tail of the linked list.
     */
    void addToList(Node<V> node) {
        if (head == null) {
            head = node;
            tail = node;
            return;
        }
        node.prevInOrder = tail;
        tail.nextInOrder = node;
        tail = node;
    }

    /**
     * Insert the node at a specific point in the linked list.
     */
    void insertIntoList(Node<V> v, Node<V> at, boolean before) {
        if (before) {
            v.nextInOrder = at;
            v.prevInOrder = at.prevInOrder;
            at.prevInOrder = v;
            if (v.prevInOrder != null) v.prevInOrder.nextInOrder = v;
            else head = v;
        } else {
            v.prevInOrder = at;
            v.nextInOrder = at.nextInOrder;
            at.nextInOrder = v;
            if (v.nextInOrder != null) v.nextInOrder.prevInOrder = v;
            else tail = v;
        }
    }
    void insertIntoListAfter(Node<V> v, Node<V> at) {
        insertIntoList(v, at, false);
    }
    void insertIntoListBefore(Node<V> v, Node<V> at) {
        insertIntoList(v, at, true);
    }

    /**
     * Remove the vertex V from the map.
     * @return the `Node<V>` that v was associated with, or null if v is not in the map.
     */
    Node<V> remove(V v) {
        int hash = hash(v);
        int i = getIndex(hash);
        Node<V> currentNode = table[i];

        // node is first in bucket
        if (currentNode != null && v.equals(currentNode.object)) {
            table[i] = currentNode.nextInBucket;
            size--;
            removeFromList(currentNode);
            return currentNode;
        }

        // find node
        Node<V> previousNode = null;
        while (currentNode != null) {
            if (v.equals(currentNode.object)) {
                if (previousNode != null) previousNode.nextInBucket = currentNode.nextInBucket;
                size--;
                removeFromList(currentNode);
                return currentNode;
            }
            previousNode = currentNode;
            currentNode = currentNode.nextInBucket;
        }

        return null;
    }

    /**
     * Remove the node from the linked list.
     */
    void removeFromList(Node<V> node) {
        if (head == node) {
            head = node.nextInOrder;
            if (head != null) head.prevInOrder = null;
            return;
        }
        if (tail == node) {
            tail = node.prevInOrder;
            if (tail != null) tail.nextInOrder = null;
            return;
        }
        node.prevInOrder.nextInOrder = node.nextInOrder;
        node.nextInOrder.prevInOrder = node.prevInOrder;
    }

    /**
     * Increase the length of the table if the size exceeds the capacity,
     * and recalculate the indices.
     */

    @SuppressWarnings("unchecked")
    boolean checkLength() {
        if (size >= threshold) {
            occupiedBuckets = 0;
            int newLength = 2 * table.length;
            Node<V>[] oldTable = table, newTable = (Node<V>[]) new Node[newLength];
            for (int i = 0; i < oldTable.length; i++) {
                if (oldTable[i] != null) {
                    Node<V> tail1 = null, tail2 = null, current = oldTable[i];
                    while (current != null) {
                        int newIndex = current.mapHash & newLength - 1;
                        if (newIndex == i) {
                            if (tail1 == null) {
                                newTable[newIndex] = current;
                                occupiedBuckets++;
                            }
                            else {
                                tail1.nextInBucket = current;
                            }
                            tail1 = current;
                        } else {
                            if (tail2 == null) {
                                newTable[newIndex] = current;
                                occupiedBuckets++;
                            }
                            else {
                                tail2.nextInBucket = current;
                            }
                            tail2 = current;
                        }
                        Node<V> next = current.nextInBucket;
                        current.nextInBucket = null;
                        current = next;

                    }
                }
            }
            threshold = (int) (RESIZE_THRESHOLD * newLength);
            table = newTable;
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    void clear() {
        table = (Node<V>[]) new Node[table.length];
        size = 0;
        occupiedBuckets = 0;
        head = null;
        tail = null;
    }

    /**
     * Get the table index of the Node<V> which has the given hash.
     */
    int getIndex(int hash) {
        return hash & table.length - 1;
    }

    /**
     * Get the hash used to calculate the index in the table at which the Node<V> associated with
     * v would be held.
     */
    static int hash(Object v) {
        final int h = v.hashCode();
        return h ^ h >>> 16;
    }

    /**
     * Iterates in the order of the linked list. Used by the vertex and Node<V> collections.
     */
    static class NodeIterator<V> implements Iterator<Node<V>> {

        final NodeMap<V> nodeMap;
        Node<V> current;

        NodeIterator(NodeMap<V> nodeMap) {
            this.nodeMap = nodeMap;
            current = nodeMap.head;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Node<V> next() {
            if(hasNext()){
                Node<V> next = current;
                current = current.nextInOrder;
                return next;
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("You cannot modify this list - use the Graph object.");
        }

    }



    // sorting
    // adapted from https://www.geeksforgeeks.org/merge-sort-for-linked-list/

    void sort(Comparator<V> comparator) {
        if (size < 2) return;

        head = mergeSort(head, comparator);

        // the sort only sets references to the next in list for each element,
        // need to iterate through and set references to previous
        Iterator<Node<V>> iterator = nodeCollection.iterator();
        Node<V> node = null, prev = null;
        while (iterator.hasNext()) {
            node = iterator.next();
            node.prevInOrder = prev;
            prev = node;
        }
        tail = node;
    }

    Node<V> mergeSort(Node<V> h, Comparator<V> comparator) {

        if (h == null || h.nextInOrder == null) return h;

        Node<V> middle = getMiddle(h);
        Node<V> middleNext = middle.nextInOrder;

        middle.nextInOrder = null;

        Node<V> left = mergeSort(h, comparator);
        Node<V> right = mergeSort(middleNext, comparator);

        return sortedMerge(left, right, comparator);
    }

    Node<V> sortedMerge(Node<V> a, Node<V> b, Comparator<V> comparator) {

        if (a == null) return b;
        if (b == null) return a;

        Node<V> newHead;

        if (comparator.compare(a.object, b.object) < 0) {
            newHead = a;
            newHead.nextInOrder = sortedMerge(a.nextInOrder, b, comparator);
        } else {
            newHead = b;
            newHead.nextInOrder = sortedMerge(a, b.nextInOrder, comparator);
        }
        return newHead;
    }

    Node<V> getMiddle(Node<V> head) {
        if (head == null) return null;

        Node<V> slow = head, fast = head;

        while (fast.nextInOrder != null && fast.nextInOrder.nextInOrder != null) {
            slow = slow.nextInOrder;
            fast = fast.nextInOrder.nextInOrder;
        }
        return slow;
    }

    //================================================================================
    // Topological sorting
    //================================================================================

    // Keep track of the current position in the linked list,
    // We traverse the graph via DFS, and when we hit a terminal node we move that node
    // to the current cursor position, then move the cursor along one.
    Node<V> cursor;

    boolean topologicalSort() {
        if (size < 2 || graph.getEdgeCount() < 1) return true;

        // start the cursor at the tail and work towards the head,
        // so the list is sorted from head to tail
        cursor = tail;

        boolean success = true;
        while (success && cursor != null) {
            success = recursiveTopologicalSort(cursor, graph.algorithms().requestRunID());
        }

        cursor = null;
        return success;
    }

    private boolean recursiveTopologicalSort(Node<V> v, int runID) {

        v.resetAlgorithmAttribs(runID);

        if (v.isProcessed()) return true;
        if (v.isSeen()) return false; // not a DAG

        v.setSeen(true);

        Deque<Connection<V>> outEdges = v.getOutEdges();
        for (Connection<V> e : outEdges) {
            if (!recursiveTopologicalSort(e.getNodeB(), runID)) return false;
        }

        v.setSeen(false);
        v.setProcessed(true);

        if (cursor != v) {
            // move v from its current position to just after the cursor
            removeFromList(v);
            insertIntoListAfter(v, cursor);
        } else {
            // v is already in the cursor position, just need to move the cursor along
            cursor = cursor.prevInOrder;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NodeMap - size: ");
        sb.append(size);
        sb.append(", table length: ");
        sb.append(table.length);
        sb.append(", occupiedBuckets: ");
        sb.append(occupiedBuckets);
        sb.append("\n");
        sb.append("--------------");
        sb.append("\n");

        for (int i = 0; i < table.length; i++) {
            sb.append(i);
            sb.append("]  ");
            Node<V> node = table[i];
            while (node != null) {
                sb.append(node);
                if (node.nextInBucket != null) sb.append(" -> ");
                node = node.nextInBucket;
            }
            sb.append("\n");
        }

        return sb.append("--------------").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeMap<?> nodeMap = (NodeMap<?>) o;

        if (size != nodeMap.size) return false;
        if (occupiedBuckets != nodeMap.occupiedBuckets) return false;
        // head and tail use reference equality, like Node usually does
        if (head != nodeMap.head) return false;
        if (tail != nodeMap.tail) return false;
        for (int i = 0; i < table.length; i++) {
            Node<V> node = table[i];
            while (node != null) {
                if(!nodeMap.contains(node)) return false;
                node = node.nextInBucket;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 421;
        for (int i = 0; i < table.length; i++) {
            Node<V> node = table[i];
            while (node != null) {
                result += hash(node);
                node = node.nextInBucket;
            }
        }
        result = 107 * result + (head != null ? head.hashCode() : 0);
        result = 107 * result + (tail != null ? tail.hashCode() : 0);
        result = 107 * result + size;
        result = 107 * result + occupiedBuckets;
        return result;
    }
}
