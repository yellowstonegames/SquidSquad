package com.github.yellowstonegames.path;

import com.github.tommyettinger.ds.IntFloatMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.Coord;

import java.io.Serializable;

/**
 * Performs A* search to find the shortest path between two Coord points.
 * <br>
 * A* is a best-first search algorithm for pathfinding. It uses a heuristic
 * value to reduce the total search space. If the heuristic is too large then
 * the optimal path is not guaranteed to be returned.
 * <br>
 * This implementation is a thin wrapper around {@link CostlyGraph} and the other code in the
 * {@code com.github.yellowstonegames.path} package; it is based on code from simple-graphs by earlygrey. The current
 * version is quite fast, typically outpacing gdx-ai's more-complex IndexedAStarPathfinder by a high margin. Typical
 * usage of this class involves either the simpler technique of only using {@code '#'} for walls or obstructions and
 * calling {@link #AStarSearch(char[][], Heuristic)}, or the more complex technique that allows variable costs for
 * different types of terrain, using {@link CostlyGraph#generateAStarCostMap(char[][], IntFloatMap, float)} to generate
 * a cost map.
 * @see CostlyGraph the pathfinding class this is based on; CostlyGraph can be used independently
 * @see DijkstraMap a sometimes-faster pathfinding algorithm that can pathfind to multiple goals
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - optimized code
 * @author earlygrey - wrote and really optimized simple-graphs, which this uses heavily
 */
public class AStarSearch implements Serializable {
    private static final long serialVersionUID = 11L;
    protected int width, height;
    protected Coord start, target;
    protected Heuristic<Coord> type;
    
    protected CostlyGraph graph;
    protected ObjectList<Coord> path;
    
    
    protected AStarSearch()
    {
        width = 0;
        height = 0;
        type = Heuristic.MANHATTAN;
    }
    /**
     * Builds a pathing object to run searches on.
     * <br>
     * Values in the map are treated as positive values being legal weights, with higher values being harder to pass
     * through. Any negative value is treated as being an impassible space. A weight of 0 can be moved through at no
     * cost, but this should be used very carefully, if at all. Cost maps are commonly built using the
     * {@link CostlyGraph#generateAStarCostMap(char[][], IntFloatMap, float)}  and
     * {@link CostlyGraph#generateAStarCostMap(char[][])} methods from a 2D char array.
     * <br>
     * If the type is Manhattan, only the cardinal directions will be used. All other search types will return a result
     * based on diagonal and cardinal pathing (8-way).
     *
     * @param map the search map, as produced by {@link CostlyGraph#generateAStarCostMap(char[][])}
     * @param type a Heuristic constant; one of the four from from {@link Heuristic#HEURISTICS}
     */
    public AStarSearch(float[][] map, Heuristic<Coord> type) {
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? Heuristic.EUCLIDEAN : type;
        graph = new CostlyGraph(map, (this.type != Heuristic.MANHATTAN));
        path = new ObjectList<>(width + height);
    }
    /**
     * Builds a pathing object to run searches on.
     * <br>
     * Values in the map are all considered equally passable unless the char is {@code '#'}, in which case it is
     * considered an impassable wall. The {@code getBareDungeon()} method in squidplace's DungeonProcessor class is a
     * common way to get a map where only '#' is used to mean a wall.
     * <br>
     * If the type is Manhattan, only the cardinal directions will be used. All other search types will return a result
     * based on diagonal and cardinal pathing (8-way).
     *
     * @param map a 2D char array where only {@code '#'} represents a wall, and anything else is equally passable
     * @param type the manner of search
     */
    public AStarSearch(char[][] map, Heuristic<Coord> type) {
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? Heuristic.EUCLIDEAN : type;
        graph = new CostlyGraph(map, (this.type != Heuristic.MANHATTAN));
        path = new ObjectList<>(width + height);
    }

    /**
     * Resets this pathing object to use a different map and optionally a different Heuristic.
     * <br>
     * Values in the map are treated as positive values being legal weights, with higher values being harder to pass
     * through. Any negative value is treated as being an impassible space. A weight of 0 can be moved through at no
     * cost, but this should be used very carefully, if at all. Cost maps are commonly built using the
     * {@link CostlyGraph#generateAStarCostMap(char[][], IntFloatMap, float)}  and
     * {@link CostlyGraph#generateAStarCostMap(char[][])} methods from a 2D char array.
     * <br>
     * If the type is Manhattan, only the cardinal directions will be used. All other search types will return a result
     * based on diagonal and cardinal pathing (8-way).
     *
     * @param map the search map, as produced by {@link CostlyGraph#generateAStarCostMap(char[][])} 
     * @param type the manner of search
     */
    public AStarSearch reinitialize(float[][] map, Heuristic<Coord> type){
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? Heuristic.EUCLIDEAN : type;
        graph.init(map, this.type != Heuristic.MANHATTAN);
        return this;
    }

    /**
     * Resets this pathing object to use a different map and optionally a different Heuristic.
     * <br>
     * Values in the map are all considered equally passable unless the char is {@code '#'}, {@code '+'}, or any box
     * drawing character, in which case it is considered an impassable wall. The {@code getBareDungeon()} method in
     * squidplace's DungeonProcessor class is a common way to get a map where only '#' is used to mean a wall.
     * <br>
     * If the type is Manhattan, only the cardinal directions will be used. All other search types will return a result
     * based on diagonal and cardinal pathing (8-way).
     *
     * @param map a 2D char array where only {@code '#'} represents a wall, and anything else is equally passable
     * @param type the manner of search
     */
    public AStarSearch reinitialize(char[][] map, Heuristic<Coord> type){
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? Heuristic.EUCLIDEAN : type;
        graph.init(CostlyGraph.generateAStarCostMap(map), this.type != Heuristic.MANHATTAN);
        return this;
    }

    /**
     * Finds an A* path to the target from the start. If no path is possible,
     * returns null. Reuses the returned path instead of allocating a new ObjectList,
     * so if you call this again, then the previously-returned List could change.
     *
     * @param startx the x coordinate of the start location
     * @param starty the y coordinate of the start location
     * @param targetx the x coordinate of the target location
     * @param targety the y coordinate of the target location
     * @return the shortest path, or null if no path is possible
     */
    public ObjectList<Coord> path(int startx, int starty, int targetx, int targety) {
        return path(Coord.get(startx, starty), Coord.get(targetx, targety));
    }
    /**
     * Finds an A* path to the target from the start. If no path is possible,
     * returns null. Reuses the returned path instead of allocating a new ObjectList,
     * so if you call this again, then the previously-returned List could change.
     *
     * @param start the start location
     * @param target the target location
     * @return the shortest path, or null if no path is possible
     */
    public ObjectList<Coord> path(Coord start, Coord target) {
        path.clear();
        this.start = start;
        this.target = target;
        if(graph.findShortestPath(start, target, path, type)) 
            return path;
        else
            return null;
    }

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
        final int w5 = width * 5;
        final char[] cs = graph.show();
        cs[start.y * w5 + start.x * 5] = cs[start.y * w5 + start.x * 5 + 1] =
                cs[start.y * w5 + start.x * 5 + 2] = cs[start.y * w5 + start.x * 5 + 3] = '@';
        cs[target.y * w5 + target.x * 5] = cs[target.y * w5 + target.x * 5 + 1] =
                cs[target.y * w5 + target.x * 5 + 2] = cs[target.y * w5 + target.x * 5 + 3] = '!';
        return String.valueOf(cs);
    }
}
