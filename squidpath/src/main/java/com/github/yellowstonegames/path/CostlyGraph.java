package com.github.yellowstonegames.path;


import com.github.tommyettinger.ds.IntFloatMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.Direction;

import java.util.Arrays;

/**
 * A default setting for a DirectedGraph of Coord vertices where each passable cell has a cost to enter it from any
 * passable neighbor.
 * <br>
 * Created by Tommy Ettinger on 7/9/2020.
 */
public class CostlyGraph extends DirectedGraph<Coord> {
	/**
	 * Given a char[][] for the map, produces a float[][] that can be used as a cost map by this class. It
	 * expects any doors to be represented by '+' if closed or '/' if open and any walls to be '#' or box drawing
	 * characters. Any wall or closed door will be assigned a negative number, meaning it is impassable for the graph
	 * search, and all other chars will be assigned 1.0, giving them a normal cost.
	 *
	 * @param map          a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors() .
	 * @return a cost map suitable for use with AStarSearch
	 */
	public static float[][] generateAStarCostMap(char[][] map) {
		int width = map.length;
		int height = map[0].length;
		float[][] portion = new float[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				switch (map[i][j]) {
					case '\1':
					case '├':
					case '┤':
					case '┴':
					case '┬':
					case '┌':
					case '┐':
					case '└':
					case '┘':
					case '│':
					case '─':
					case '┼':
					case '#':
					case '+':
						portion[i][j] = -1;
						break;
					default:
						portion[i][j] = 1;
				}
			}
		}
		return portion;
	}

	/**
	 * Given a char[][] for the map, a jdkgdxds IntFloatMap that maps chars in the map to their costs, and a float value
	 * for unhandled characters, produces a float[][] that can be used as a map by AStarSearch. It expects any doors to
	 * be represented by '+' if closed or '/' if open, and any walls to be '#' or box drawing characters. In the
	 * parameter costs, there does not need to be an entry for '#' or any box drawing characters, but if one is present
	 * for '#' it will apply that cost to both '#' and all box drawing characters, and if one is not present it will
	 * default to a negative number, meaning it is impassable for a graph search. For any other entry in costs, a char
	 * in the 2D char array that matches the key (which is an int, but can and should be in the valid char range) will
	 * correspond (at the same x,y position in the returned 2D float array) to that key's value in costs. If a char is
	 * used in the map but does not have a corresponding key in costs, it will be given the value of the parameter
	 * defaultValue, which is typically 1 unless a creature is limited to only moving in some terrain.
	 * <p/>
	 * The values in costs are different from those expected for DijkstraMap; negative numbers are impassable, 1 is the
	 * cost for a normal walkable tile, and higher numbers are harder to enter.
	 * <p/>
	 * An example use for this would be to make a creature unable to enter any non-water cell (like a fish),
	 * unable to enter doorways (like some mythological versions of vampires), or to make a wheeled vehicle take more
	 * time to move across rubble or rough terrain.
	 * <p/>
	 * A potentially common case that needs to be addressed is NPC movement onto staircases in games that have them;
	 * some games may find it desirable for NPCs to block staircases and others may not, but in either case you should
	 * give both '&gt;' and '&lt;', the standard characters for staircases, the same value in costs.
	 *
	 * @param map          a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors() .
	 * @param costs        a Map of Character keys representing possible elements in map, and Double values for their cost.
	 * @param defaultValue a float that will be used as the cost for any characters that don't have a key in costs.
	 * @return a cost map suitable for use with AStarSearch
	 */
	public static float[][] generateAStarCostMap(char[][] map, IntFloatMap costs, float defaultValue) {
		int width = map.length;
		int height = map[0].length;
		float[][] portion = new float[width][height];
		char current;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				current = map[i][j];
				if (costs.containsKey(current)) {
					portion[i][j] = costs.get(current);
				} else {
					switch (current) {
						case '\1':
						case '├':
						case '┤':
						case '┴':
						case '┬':
						case '┌':
						case '┐':
						case '└':
						case '┘':
						case '│':
						case '─':
						case '┼':
						case '#':
							portion[i][j] = costs.getOrDefault('#', -1);
							break;
						default:
							portion[i][j] = defaultValue;
					}
				}
			}
		}
		return portion;
	}


	public int width;
	public int height;

	/**
	 * No-op no-arg constructor, present for serialization; if you use this you must call
	 * {@link #init(float[][])} or {@link #init(float[][], boolean)} before using the CostlyGraph.
	 */
	public CostlyGraph() {
		super();
		width = 0;
		height = 0;
	}

	/**
	 * Builds a DefaultGraph from a 2D float array that uses negative numbers to represent any kind of inaccessible
	 * cell, with all other numbers treated as possible to enter for a cost equal to that float. This only builds
	 * connections along cardinal directions.
	 * @see #generateAStarCostMap(char[][]) This method can generate a suitable cost map from a char[][] map.
	 * @param map a 2D float array where negative numbers are impassable and non-negative ones represent costs to enter
	 */
	public CostlyGraph(float[][] map) {
		this(map, false);
	}

	/**
	 * Builds a DefaultGraph from a 2D float array that uses negative numbers to represent any kind of inaccessible
	 * cell, with all other numbers treated as possible to enter for a cost equal to that float. If {@code eightWay} is
	 * true, this builds connections along diagonals as well as along cardinals, but if {@code eightWay} is false, it
	 * only builds connections along cardinal directions.
	 * @see #generateAStarCostMap(char[][]) This method can generate a suitable cost map from a char[][] map.
	 * @param map a 2D float array where negative numbers are impassable and non-negative ones represent costs to enter
	 * @param eightWay if true, this will build connections on diagonals as well as cardinal directions; if false, this will only use cardinal connections
	 */
	public CostlyGraph(float[][] map, boolean eightWay) {
		super();
		init(map, eightWay);
	}

	/**
	 * Builds a DefaultGraph from a 2D char array that treats {@code '#'}, {@code '+'}, and all box drawing characters
	 * as impassable, but considers all other cells passable for a cost of 1.0. This only builds connections along
	 * cardinal directions.
	 * <br>
	 * This simply delegates to {@link #init(float[][], boolean)} with the result of
	 * {@link #generateAStarCostMap(char[][])} for the 2D float array. You can get more control by
	 * calling {@link #generateAStarCostMap(char[][], IntFloatMap, float)} and passing that to init() or a
	 * constructor that takes a 2D float array.
	 *
	 * @param map a 2D char array where {@code '#'}, {@code '+'}, and all box drawing characters are considered impassable
	 */
	public CostlyGraph(char[][] map) {
		super();
		init(generateAStarCostMap(map), false);
	}

	/**
	 * Builds a DefaultGraph from a 2D char array that treats {@code '#'}, {@code '+'}, and all box drawing characters
	 * as impassable, but considers all other cells passable for a cost of 1.0. If {@code eightWay} is true, this builds
	 * connections along diagonals as well as along cardinals, but if {@code eightWay} is false, it only builds
	 * connections along cardinal directions.
	 * <br>
	 * This simply delegates to {@link #init(float[][], boolean)} with the result of
	 * {@link #generateAStarCostMap(char[][])} for the 2D float array. You can get more control by
	 * calling {@link #generateAStarCostMap(char[][], IntFloatMap, float)} and passing that to init() or a
	 * constructor that takes a 2D float array.
	 *
	 * @param map a 2D char array where {@code '#'}, {@code '+'}, and all box drawing characters are considered impassable
	 * @param eightWay if true, this will build connections on diagonals as well as cardinal directions; if false, this will only use cardinal connections
	 */
	public CostlyGraph(char[][] map, boolean eightWay) {
		super();
		init(generateAStarCostMap(map), eightWay);
	}

	/**
	 * Re-initializes this DefaultGraph from a 2D float array that uses negative numbers to represent any kind of
	 * inaccessible cell, with all other numbers treated as possible to enter for a cost equal to that float. This only
	 * builds connections along cardinal directions.
	 *
	 * @see #generateAStarCostMap(char[][], IntFloatMap, float) This method can generate this type of map.
	 * @param map a 2D float array where negative numbers are impassable and non-negative ones represent costs to enter
	 */
	public void init(float[][] map) {
		init(map, false);
	}
	/**
	 * Re-initializes this DefaultGraph from a 2D float array that uses negative numbers to represent any kind of
	 * inaccessible cell, with all other numbers treated as possible to enter for a cost equal to that float. If
	 * {@code eightWay} is true, this builds connections along diagonals as well as along cardinals, but if
	 * {@code eightWay} is false, it only builds connections along cardinal directions.
	 *
	 * @see #generateAStarCostMap(char[][], IntFloatMap, float) This can generate this type of map.
	 * @param map a 2D float array where negative numbers are impassable and non-negative ones represent costs to enter
	 * @param eightWay if true, this will build connections on diagonals as well as cardinal directions; if false, this will only use cardinal connections
	 */
	public void init(float[][] map, boolean eightWay) {
		width = map.length;
		height = map[0].length;
		Coord.expandPoolTo(width, height);
		ObjectList<Coord> vs = new ObjectList<>(width * height >>> 1);
		vertexMap.clear();
		edgeMap.clear();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if(map[x][y] >= 0.0)
				{
					Coord pt = Coord.get(x, y);
					vs.add(pt);
					addVertex(pt);
				}
			}
		}
		final int sz = vs.size();
		Coord center, off;
		final Direction[] outer = eightWay ? Direction.CLOCKWISE : Direction.CARDINALS_CLOCKWISE;
		Direction dir;
		for (int i = 0; i < sz; i++) {
			center = vs.get(i);
			for (int j = 0; j < outer.length; j++) {
				dir = outer[j];
				off = center.translate(dir);
				if(off.isWithin(width, height) && map[center.x + dir.deltaX][center.y + dir.deltaY] >= 0.0)
				{
					addEdge(off, center, (float)map[center.x][center.y]);
				}
			}
		}
	}

	/**
	 * Sort the vertices of this graph in topological order. That is, for every edge from vertex u to vertex v, u comes
	 * before v in the ordering. This is reflected in the iteration order of the collection returned by
	 * {@link Graph#getVertices()}. Note that the graph cannot contain any cycles for a topological order to exist. If a
	 * cycle exists, this method will do nothing.
	 * @return true if the sort was successful, false if the graph contains a cycle
	 */
	public boolean topologicalSort() {
		return algorithms.topologicalSort();
	}

	/**
	 * Perform a topological sort on the graph, and puts the sorted vertices in the supplied list.
	 * That is, for every edge from vertex u to vertex v, u will come before v in the supplied list.
	 * Note that the graph cannot contain any cycles for a topological order to exist. If a cycle exists, the sorting
	 * procedure will terminate and the supplied list will only contain the vertices up until the point of termination.
	 * @param sortedVertices an ObjectList of V vertices that will be cleared and modified in-place
	 * @return true if the sort was successful, false if the graph contains a cycle
	 */
	public boolean topologicalSort(ObjectList<Coord> sortedVertices) {
		return algorithms.topologicalSort(sortedVertices);
	}

	/**
	 * Find the shortest path between the start and target vertices, using Dijkstra's algorithm implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
	 */
	public ObjectList<Coord> findShortestPath(Coord start, Coord target) {
		return algorithms.findShortestPath(start, target);
	}

	/**
	 * Find the shortest path between the start and target vertices, using the A* search algorithm with the provided heuristic, and implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @param heuristic typically predefined in {@link Heuristic}, this determines how the optimal path will be estimated
	 * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
	 */
	public ObjectList<Coord> findShortestPath(Coord start, Coord target, Heuristic<Coord> heuristic) {
		return algorithms.findShortestPath(start, target, heuristic);
	}

	/**
	 * Find the shortest path between the start and target vertices, using the A* search algorithm with the provided
	 * heuristic, and implemented with a priority queue. Fills path with a list of vertices from start to target
	 * containing the ordered vertices of a shortest path, including both the start and target vertices.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @param path the list of vertices to which the path vertices should be added
	 * @return true if a path was found, or false if no path could be found
	 */
	public boolean findShortestPath(Coord start, Coord target, ObjectList<Coord> path, Heuristic<Coord> heuristic) {
		return algorithms.findShortestPath(start, target, path, heuristic);
	}

	/**
	 * Find the shortest path between the start and target vertices, using Dijkstra's algorithm implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @return the sum of the weights in a shortest path from the starting vertex to the target vertex
	 */
	public float findMinimumDistance(Coord start, Coord target) {
		return algorithms.findMinimumDistance(start, target);
	}

	/**
	 * Perform a breadth first search starting from the specified vertex.
	 * @param coord the vertex at which to start the search
	 * @param maxVertices the maximum number of vertices to process before terminating the search
	 * @param maxDepth the maximum edge distance (the number of edges in a shortest path between vertices) a vertex should have to be
	 *                 considered for processing. If a vertex has a distance larger than the maxDepth, it will not be added to the
	 *                 returned graph
	 * @return a Graph object containing all the processed vertices, and the edges from which each vertex was encountered.
	 * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
	 * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
	 */
	public Graph<Coord> breadthFirstSearch(Coord coord, int maxVertices, int maxDepth) {
		return algorithms.breadthFirstSearch(coord, maxVertices, maxDepth);
	}

	/**
	 * Perform a breadth first search starting from the specified vertex.
	 * @param coord the vertex at which to start the search
	 * @return a Graph object containing all the processed vertices (all the vertices in this graph), and the edges from which each vertex was encountered.
	 * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
	 * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
	 */
	public Graph<Coord> breadthFirstSearch(Coord coord) {
		return algorithms.breadthFirstSearch(coord);
	}

	/**
	 * Perform a depth first search starting from the specified vertex.
	 * @param coord the vertex at which to start the search
	 * @param maxVertices the maximum number of vertices to process before terminating the search
	 * @param maxDepth the maximum edge distance (the number of edges in a shortest path between vertices) a vertex should have to be
	 *                 considered for processing. If a vertex has a distance larger than the maxDepth, it will not be added to the
	 *                 returned graph
	 * @return a Graph object containing all the processed vertices, and the edges from which each vertex was encountered.
	 * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
	 * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
	 */
	public Graph<Coord> depthFirstSearch(Coord coord, int maxVertices, int maxDepth) {
		return algorithms.depthFirstSearch(coord, maxVertices, maxDepth);
	}

	/**
	 * Perform a depth first search starting from the specified vertex.
	 * @param coord the vertex at which to start the search
	 * @return a Graph object containing all the processed vertices (all the vertices in this graph), and the edges from which each vertex was encountered.
	 * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
	 * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
	 */
	public Graph<Coord> depthFirstSearch(Coord coord) {
		return algorithms.depthFirstSearch(coord);
	}

	/**
	 * Checks whether there are any cycles in the graph using depth first searches.
	 * @return true if the graph contains a cycle, false otherwise
	 */
	public boolean detectCycle() {
		return algorithms.detectCycle();
	}

	/**
	 * Creates a 1D char array (which can be passed to {@link String#valueOf(char[])}) filled with a grid made of the
	 * vertices in this Graph and their estimated costs, if this has done an estimate. Each estimate is rounded to the
	 * nearest int and only printed if it is 4 digits or less; otherwise this puts '####' in the grid cell. This is a
	 * building-block for toString() implementations that may have debugging uses as well.
	 * @return a 1D char array containing newline-separated rows of space-separated grid cells that contain estimated costs or '####' for unexplored
	 */
	public char[] show() {
		final int w5 = width * 5;
		final char[] cs = new char[w5 * height];
		Arrays.fill(cs,  '#');
		for (int i = 4; i < cs.length; i += 5) {
			cs[i] = (i + 1) % w5 == 0 ? '\n' : ' ';
		}
		final int vs = vertexMap.size(), rid = algorithms.lastRunID();
		Node<Coord> nc;
		for (int i = 0; i < vs; i++) {
			nc = vertexMap.getAt(i);
			if(!nc.seen || nc.lastRunID != rid || nc.distance >= 9999.5)
				continue;
			int d = (int) (nc.distance + 0.5), x = nc.object.x * 5, y = nc.object.y;
			cs[y * w5 + x    ] = (d >= 1000) ? (char) ('0' + d / 1000) : ' ';
			cs[y * w5 + x + 1] = (d >= 100)  ? (char) ('0' + d / 100 % 10) : ' ';
			cs[y * w5 + x + 2] = (d >= 10)   ? (char) ('0' + d / 10 % 10) : ' ';
			cs[y * w5 + x + 3] = (char) ('0' + d % 10);
		}
		return cs;
	}

}
