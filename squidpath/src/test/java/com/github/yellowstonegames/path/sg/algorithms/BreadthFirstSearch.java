package com.github.yellowstonegames.path.sg.algorithms;

import java.util.ArrayDeque;

import com.github.yellowstonegames.path.sg.Array;
import com.github.yellowstonegames.path.sg.Connection;
import com.github.yellowstonegames.path.sg.Node;
import com.github.yellowstonegames.path.sg.algorithms.Algorithm;
import com.github.yellowstonegames.path.sg.algorithms.SearchStep;
import com.github.yellowstonegames.path.sg.utils.SearchProcessor;

public class BreadthFirstSearch<V> extends Algorithm<V> {

    private SearchProcessor<V> processor;
    private SearchStep<V> step = new SearchStep<>();

    private final ArrayDeque<Node<V>> queue;

    public BreadthFirstSearch(int id, Node<V> start, SearchProcessor<V> processor) {
        super(id);
        this.processor = processor;
        queue = new ArrayDeque<>();
        start.resetAlgorithmAttribs(id);
        queue.add(start);
        start.setSeen(true);
    }

    @Override
    public boolean update() {
        if (isFinished()) return true;

        Node<V> v = queue.pollFirst();
        if (processor != null) {
            step.prepare(v);
            processor.accept(step);
            if (step.terminate) {
                queue.clear();
                return true;
            }
            if (step.ignore) return isFinished();
        }
        Array<Connection<V>> outEdges = v.getOutEdges();
        for (Connection<V> e : outEdges) {
            Node<V> w = e.getNodeB();
            w.resetAlgorithmAttribs(id);
            if (!w.isSeen()) {
                w.setIndex(v.getIndex() + 1);
                w.setDistance(v.getDistance() + e.getWeight());
                w.setConnection(e);
                w.setSeen(true);
                queue.addLast(w);
            }
        }
        return isFinished();
    }

    @Override
    public boolean isFinished() {
        return queue.isEmpty();
    }

}