package com.github.yellowstonegames.path;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.tommyettinger.ds.support.FourWheelRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordOrderedSet;
import com.github.yellowstonegames.grid.Direction;
import com.github.yellowstonegames.grid.WobblyLine;

import javax.annotation.Nonnull;

/**
 * Like {@link WobblyLine}, this generates orthogonally-connected paths of {@link Coord} that meander through an area;
 * unlike WobblyLine, this won't ever generate paths that cross themselves.
 * <br>
 * This generates a fully-connected graph for a given rectangular area, then solves it with
 * {@link DefaultGraph#findShortestPath(Coord, Coord, ObjectList, Heuristic)}.
 * <br>
 * Created by Tommy Ettinger on 6/26/2020.
 */
public class TwistedLine {
    @Nonnull
    public EnhancedRandom rng;
    @Nonnull
    public final DefaultGraph graph;
    @Nonnull
    public final ObjectList<Coord> lastPath;

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
        this.rng = rng == null ? new FourWheelRandom() : rng;
        lastPath = new ObjectList<>(graph.width + graph.height);
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

            for (Direction dir : dirs) {
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

    public ObjectList<Coord> line(int startX, int startY, int endX, int endY) {
        return line(Coord.get(startX, startY), Coord.get(endX, endY));
    }

    public ObjectList<Coord> line(Coord start, Coord end) {
        graph.findShortestPath(start, end, lastPath, Heuristic.EUCLIDEAN);
        return lastPath;
    }

    public int getWidth() {
        return graph.width;
    }

    public int getHeight() {
        return graph.height;
    }

    @Nonnull
    public EnhancedRandom getRng() {
        return rng;
    }

    public void setRng(EnhancedRandom rng) {
        this.rng = rng == null ? new FourWheelRandom() : rng;
    }

    /**
     * Gets the last path this found, which may be empty. This returns the same reference to any path this produces,
     * and the path is cleared when a new twisted line is requested. You probably want to copy the contents of this path
     * into another list if you want to keep its contents.
     * @return the most recent path of Coord, as an ObjectList, this found.
     */
    @Nonnull
    public ObjectList<Coord> getLastPath() {
        return lastPath;
    }
}