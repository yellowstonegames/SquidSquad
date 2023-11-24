package com.github.yellowstonegames.path.sg.algorithms;

import com.github.tommyettinger.ds.BinaryHeap;
import com.github.yellowstonegames.path.sg.Array;
import com.github.yellowstonegames.path.sg.Connection;
import com.github.yellowstonegames.path.sg.Node;
import com.github.yellowstonegames.path.sg.Path;
import com.github.yellowstonegames.path.sg.algorithms.Algorithm;
import com.github.yellowstonegames.path.sg.algorithms.AlgorithmPath;
import com.github.yellowstonegames.path.sg.algorithms.SearchStep;
import com.github.yellowstonegames.path.sg.utils.Heuristic;
import com.github.yellowstonegames.path.sg.utils.SearchProcessor;

public class AStarSearch<V> extends Algorithm<V> {

    private Heuristic<V> heuristic;
    private SearchProcessor<V> processor;
    private SearchStep<V> step = new SearchStep<>();
    private final BinaryHeap<Node<V>> heap;
    private Node<V> start, target, u, end;
    private Path<V> path;

    protected AStarSearch(int id, final Node<V> start, final Node<V> target, final Heuristic<V> heuristic, final SearchProcessor<V> processor) {
        super(id);
        this.start = start;
        this.target = target;
        this.heuristic = heuristic;
        this.processor = processor;
        heap = new BinaryHeap<>();
        start.resetAlgorithmAttribs(id);
        start.setDistance(0);
        heap.add(start);
    }

    @Override
    public boolean update() {
        if (isFinished()) return true;

        u = heap.pop();

        if (!u.isProcessed()) {
            if (processor != null && u.getIndex() > 0) {
                step.prepare(u);
                processor.accept(step);
                if (step.terminate) {
                    heap.clear();
                    return true;
                }
                if (step.ignore) {
                    return isFinished();
                }
            }
            u.setProcessed(true);

            if (u == target) {
                heap.clear();
                end = u;
                return true;
            }

            Array<Connection<V>> outEdges = u.getOutEdges();
            for (Connection<V> e : outEdges) {
                Node<V> v = e.getNodeB();
                v.resetAlgorithmAttribs(id);
                if (!v.isProcessed()) {
                    float newDistance = u.getDistance() + e.getWeight();
                    if (newDistance < v.getDistance()) {
                        v.setDistance(newDistance);
                        v.setPrev(u);
                        v.setConnection(e);
                        if (heuristic != null && !v.isSeen()) {
                            v.setEstimate(heuristic.getEstimate(v.getObject(), target.getObject()));
                        }
                        if (!v.isSeen()) {
                            heap.add(v, v.getDistance() + v.getEstimate());
                        } else {
                            heap.setValue(v, v.getDistance() + v.getEstimate());
                        }
                        v.setIndex(u.getIndex() + 1);
                        v.setSeen(true);
                    }
                }
            }
        }
        return isFinished();
    }

    @Override
    public boolean isFinished() {
        return heap.size == 0;
    }

    public Path<V> getPath() {
        if (!isFinished()) return null;
        if (path == null) {
            path = end != null ? new AlgorithmPath<>(end) : Path.EMPTY_PATH;
        }
        return path;
    }

    Node<V> getEnd() {
        return end;
    }

    /**
     *
     * @param start
     * @param target
     * @param heuristic
     * @return the target Node if reachable, otherwise null
     */
    private Node<V> aStarSearch(final Node<V> start, final Node<V> target, final Heuristic<V> heuristic, final SearchProcessor<V> processor) {

        start.resetAlgorithmAttribs(id);
        start.setDistance(0);

        heap.add(start);

        final SearchStep<V> step = processor != null ? new SearchStep<>() : null;

        while(heap.size != 0) {
            Node<V> u = heap.pop();
            if (u == target) {
                heap.clear();
                return u;
            }
            if (!u.isProcessed()) {
                if (processor != null && u.getIndex() > 0) {
                    step.prepare(u);
                    processor.accept(step);
                    if (step.terminate) {
                        heap.clear();
                        return null;
                    }
                    if (step.ignore) continue;
                }
                u.setProcessed(true);
                Array<Connection<V>> outEdges = u.getOutEdges();
                for (Connection<V> e : outEdges) {
                    Node<V> v = e.getNodeB();
                    v.resetAlgorithmAttribs(id);
                    if (!v.isProcessed()) {
                        float newDistance = u.getDistance() + e.getWeight();
                        if (newDistance < v.getDistance()) {
                            v.setDistance(newDistance);
                            v.setPrev(u);
                            v.setConnection(e);
                            if (heuristic != null && !v.isSeen()) {
                                v.setEstimate(heuristic.getEstimate(v.getObject(), target.getObject()));
                            }
                            if (!v.isSeen()) {
                                heap.add(v, v.getDistance() + v.getEstimate());
                            } else {
                                heap.setValue(v, v.getDistance() + v.getEstimate());
                            }
                            v.setIndex(u.getIndex() + 1);
                            v.setSeen(true);
                        }
                    }
                }
            }
        }
        heap.clear();
        return null;
    }
}
