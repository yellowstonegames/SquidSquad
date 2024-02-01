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

import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.PouchRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordOrderedSet;
import com.github.yellowstonegames.grid.Direction;
import com.github.yellowstonegames.grid.DrunkenWalk;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Like {@link DrunkenWalk}, this generates orthogonally-connected paths of {@link Coord} that meander through an area;
 * unlike DrunkenWalk, this won't ever generate paths that cross themselves.
 * <br>
 * This generates a fully-connected graph for a given rectangular area, then solves it with
 * {@link DefaultGraph#findShortestPath(Coord, Coord, ObjectDeque, Heuristic)}.
 */
public class TwistedLine {
    @NonNull
    public EnhancedRandom rng;
    @NonNull
    public final DefaultGraph graph;
    @NonNull
    public final ObjectDeque<Coord> lastPath;

    public TwistedLine() {
        this(40, 40, null);
    }

    public TwistedLine(int width, int height) {
        this(width, height, null);
    }

    public TwistedLine(int width, int height, EnhancedRandom rng) {
        graph = new DefaultGraph();
        graph.width = Math.max(width, 2);
        graph.height = Math.max(height, 2);
        this.rng = rng == null ? new PouchRandom() : rng;
        lastPath = new ObjectDeque<>(graph.width + graph.height);
        reinitialize();
    }

    /**
     * Called automatically during construction, this sets up a random maze as a {@link DefaultGraph} so a path can be
     * found. You can call this after construction to change the paths this can find.
     */
    public void reinitialize() {
        graph.removeAllVertices();
        for (int x = 0; x < graph.width; x++) {
            for (int y = 0; y < graph.height; y++) {
                graph.addVertex(Coord.get(x, y));
            }
        }

        int x = rng.nextInt(graph.width);
        int y = rng.nextInt(graph.height);

        CoordOrderedSet deck = new CoordOrderedSet();
        deck.add(Coord.get(x, y));

        Direction[] dirs = new Direction[4];
        System.arraycopy(Direction.CARDINALS, 0, dirs, 0, 4);
        OUTER:
        while (!deck.isEmpty()) {
            int i = deck.size() - 1;
            Coord p = deck.getAt(i);
            rng.shuffle(dirs);

            for (int j = 0; j < dirs.length; j++) {
                Direction dir = dirs[j];
                x = p.x + dir.deltaX;
                y = p.y + dir.deltaY;
                if (x >= 0 && x < graph.width && y >= 0 && y < graph.height) {
                    Coord c = Coord.get(x, y);
                    if (graph.getEdges(c).isEmpty() && deck.add(c)) {
                        graph.addEdge(p, c);
                        continue OUTER;
                    }
                }
            }

            deck.remove(p);
        }

    }

    public ObjectDeque<Coord> line(int startX, int startY, int endX, int endY) {
        return line(Coord.get(startX, startY), Coord.get(endX, endY));
    }

    public ObjectDeque<Coord> line(Coord start, Coord end) {
        graph.findShortestPath(start, end, lastPath, Heuristic.EUCLIDEAN);
        return lastPath;
    }

    public int getWidth() {
        return graph.width;
    }

    public int getHeight() {
        return graph.height;
    }

    @NonNull
    public EnhancedRandom getRng() {
        return rng;
    }

    public void setRng(EnhancedRandom rng) {
        this.rng = rng == null ? new PouchRandom() : rng;
    }

    /**
     * Gets the last path this found, which may be empty. This returns the same reference to any path this produces,
     * and the path is cleared when a new twisted line is requested. You probably want to copy the contents of this path
     * into another list if you want to keep its contents.
     * @return the most recent path of Coord, as an ObjectDeque, this found.
     */
    @NonNull
    public ObjectDeque<Coord> getLastPath() {
        return lastPath;
    }
}
