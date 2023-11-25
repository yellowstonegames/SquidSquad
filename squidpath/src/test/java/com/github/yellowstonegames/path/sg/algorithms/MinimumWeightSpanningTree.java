package com.github.yellowstonegames.path.sg.algorithms;

import java.util.*;
import java.util.stream.Collectors;

import com.github.yellowstonegames.path.sg.Connection;
import com.github.yellowstonegames.path.sg.Edge;
import com.github.yellowstonegames.path.sg.Node;
import com.github.yellowstonegames.path.sg.UndirectedGraph;
import com.github.yellowstonegames.path.sg.algorithms.Algorithm;

public class MinimumWeightSpanningTree<V> extends Algorithm<V> {

    private UndirectedGraph<V> spanningTree;
    private Queue<Connection<V>> edgeQueue;
    private int finishAt;

    // adapted from https://www.baeldung.com/java-spanning-trees-kruskal

    protected MinimumWeightSpanningTree(int id, UndirectedGraph<V> graph, boolean minSpanningTree) {
        super(id);

        spanningTree = graph.createNew();

        spanningTree.addVertices(graph.getVertices());

        List<Connection<V>> toSort = new ArrayList<>();
        for (Connection<V> vConnection : graph.internals().getConnections()) {
            toSort.add(vConnection);
        }
        toSort.sort(minSpanningTree ? Comparator.comparing(Edge<V>::getWeight) : Comparator.comparing(Edge<V>::getWeight).reversed());
        ArrayDeque<Connection<V>> connections = new ArrayDeque<>();
        for (Connection<V> vConnection : toSort) {
            connections.add(vConnection);
        }
        edgeQueue = connections;

        finishAt = graph.isConnected() ? graph.size() - 1 : -1;
    }

    @Override
    public boolean update() {
        if (isFinished()) return true;

        Connection<V> edge = edgeQueue.poll();

        if (doesEdgeCreateCycle(edge.getNodeA(), edge.getNodeB(), id)) {
            return false;
        }
        spanningTree.addEdge(edge.getA(), edge.getB(), edge.getWeightFunction());

        return isFinished();
    }

    private void unionByRank(Node<V> rootU, Node<V> rootV) {
        if (rootU.getIndex() < rootV.getIndex()) {
            rootU.setPrev(rootV);
        } else {
            rootV.setPrev(rootU);
            if (rootU.getIndex() == rootV.getIndex()) rootU.setIndex(rootU.getIndex() + 1);
        }
    }

    private Node<V> find(Node<V> node) {
        if (node.equals(node.getPrev())) {
            return node;
        } else {
            return find(node.getPrev());
        }
    }
    private Node<V> pathCompressionFind(Node<V> node) {
        if (node.equals(node.getPrev())) {
            return node;
        } else {
            Node<V> parentNode = find(node.getPrev());
            node.setPrev(parentNode);
            return parentNode;
        }
    }

    private boolean doesEdgeCreateCycle(Node<V> u, Node<V> v, int runID) {
        if (u.resetAlgorithmAttribs(runID)) u.setPrev(u);
        if (v.resetAlgorithmAttribs(runID)) v.setPrev(v);
        Node<V> rootU = pathCompressionFind(u);
        Node<V> rootV = pathCompressionFind(v);
        if (rootU.equals(rootV)) {
            return true;
        }
        unionByRank(rootU, rootV);
        return false;
    }

    @Override
    public boolean isFinished() {
        return finishAt < 0 ? edgeQueue.isEmpty() : spanningTree.getEdgeCount() == finishAt;
    }

    public UndirectedGraph<V> getSpanningTree() {
        return spanningTree;
    }
}
