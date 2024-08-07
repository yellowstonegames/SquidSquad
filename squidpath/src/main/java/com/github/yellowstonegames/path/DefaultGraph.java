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
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordObjectOrderedMap;
import com.github.yellowstonegames.grid.Direction;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * A default setting for an {@link UndirectedGraph} of Coord vertices where all connections have cost 1. This should be
 * initialized with a 2D (rectangular) char array using the map convention where {@code '#'} is a wall and anything else
 * is passable.
 */
public class DefaultGraph extends UndirectedGraph<Coord>{
	public int width;
	public int height;

	/**
	 * No-op no-arg constructor, present for serialization; if you use this you must call {@link #init(char[][])}
	 * or {@link #init(char[][], boolean)} before using the DefaultGraph.
	 */
	public DefaultGraph() {
		vertexMap = new CoordObjectOrderedMap<>();
		edgeSet = new ObjectOrderedSet<>();
		algorithms = new UndirectedGraphAlgorithms<>(this);
		width = 0;
		height = 0;
	}
	/**
	 * Builds a DefaultGraph from a 2D char array that uses {@code '#'} to represent any kind of inaccessible cell, with
	 * all other chars treated as walkable. This only builds connections along cardinal directions.
	 * @param map a 2D char array where {@code '#'} represents an inaccessible area (such as a wall) and anything else is walkable
	 */
	public DefaultGraph(char[][] map) {
		this(map, false);
	}

	/**
	 * Builds a DefaultGraph from a 2D char array that uses {@code '#'} to represent any kind of inaccessible cell, with
	 * all other chars treated as walkable. If {@code eightWay} is true, this builds connections along diagonals as well
	 * as along cardinals, but if {@code eightWay} is false, it only builds connections along cardinal directions.
	 * @param map a 2D char array where {@code '#'} represents an inaccessible area (such as a wall) and anything else is walkable
	 * @param eightWay if true, this will build connections on diagonals as well as cardinal directions; if false, this will only use cardinal connections
	 */
	public DefaultGraph(char[][] map, boolean eightWay) {
		vertexMap = new CoordObjectOrderedMap<>();
		edgeSet = new ObjectOrderedSet<>();
		algorithms = new UndirectedGraphAlgorithms<>(this);
		init(map, eightWay);
	}

	/**
	 * Re-initializes a DefaultGraph from a 2D char array that uses {@code '#'} to represent any kind of inaccessible
	 * cell, with all other chars treated as walkable. This only builds connections along cardinal directions.
	 * @param map a 2D char array where {@code '#'} represents an inaccessible area (such as a wall) and anything else is walkable
	 */
	public void init(char[][] map) {
		init(map, false);
	}

	/**
	 * Re-initializes this DefaultGraph from a 2D char array that uses {@code '#'} to represent any kind of inaccessible
	 * cell, with all other chars treated as walkable. If {@code eightWay} is true, this builds connections along
	 * diagonals as well as along cardinals, but if {@code eightWay} is false, it only builds connections along cardinal
	 * directions.
	 * @param map a 2D char array where {@code '#'} represents an inaccessible area (such as a wall) and anything else is walkable
	 * @param eightWay if true, this will build connections on diagonals as well as cardinal directions; if false, this will only use cardinal connections
	 */
	public void init(char[][] map, boolean eightWay) {
		width = map.length;
		height = map[0].length;
		Coord.expandPoolTo(width, height);
		ObjectList<Coord> vs = new ObjectList<>(width * height >>> 1);
		vertexMap.clear();
		edgeSet.clear();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if(map[x][y] != '#')
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
				if(off.isWithin(width, height) && map[center.x + dir.deltaX][center.y + dir.deltaY] != '#')
				{
					if(!edgeExists(center, off))
					{
						addEdge(center, off);
					}
				}
			}
		}
	}

	/**
	 * Find a minimum weight spanning tree using Kruskal's algorithm.
	 * @return a Graph object containing a minimum weight spanning tree (if this graph is connected -
	 * in general a minimum weight spanning forest)
	 */
	public Graph<Coord> findMinimumWeightSpanningTree() {
		return algorithms.findMinimumWeightSpanningTree();
	}

	/**
	 * Find the shortest path between the start and target vertices, using Dijkstra's algorithm implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
	 */
	public ObjectDeque<Coord> findShortestPath(Coord start, Coord target) {
		return algorithms.findShortestPath(start, target);
	}

	/**
	 * Find the shortest path between the start and target vertices, using the A* search algorithm with the provided heuristic, and implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @param heuristic typically predefined in {@link Heuristic}, this determines how the optimal path will be estimated
	 * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
	 */
	public ObjectDeque<Coord> findShortestPath(Coord start, Coord target, Heuristic<Coord> heuristic) {
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
	public boolean findShortestPath(Coord start, Coord target, ObjectDeque<Coord> path, Heuristic<Coord> heuristic) {
		return algorithms.findShortestPath(start, target, path, heuristic);
	}

	/**
	 * Find the shortest path between the start and target vertices, using Dijkstra's algorithm implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @return the sum of the weights in a shortest path from the starting vertex to the target vertex
	 */
	public double findMinimumDistance(Coord start, Coord target) {
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
	public boolean containsCycle() {
		return algorithms.containsCycle();
	}

	/**
	 * Meant for serialization using <a href="https://fury.apache.org">Fury</a>.
	 * If a class overrides this with different behavior, {@link #readExternal(ObjectInput)}
	 * must also be overridden to match that behavior.
	 *
	 * @param out the stream to write the object to
	 * @throws IOException Includes any I/O exceptions that may occur
	 * @serialData <ul>
	 * <li>int width: the x-size of the grid the graph uses</li>
	 * <li>int height: the y-size of the grid the graph uses</li>
	 * <li>int nv: the number of vertices</li>
	 * <li>object[nv] vertices: a sequence of vertex objects, with count equal to nv</li>
	 * <li>int ne: the number of edges</li>
	 * <li>triple[ne] edges: interleaved in a flat sequence; for each triple:
	 *     <ul>
	 *         <li>object vertexA</li>
	 *         <li>object vertexB</li>
	 *         <li>float weight</li>
	 *     </ul>
	 *     </li>
	 * </ul>
	 */
	@GwtIncompatible
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(width);
		out.writeInt(height);
		super.writeExternal(out);
	}

	/**
	 * Meant for deserialization using <a href="https://fury.apache.org">Fury</a>.
	 * If a class overrides this with different behavior, {@link #writeExternal(ObjectOutput)}
	 * must also be overridden to match that behavior.
	 *
	 * @param in the stream to read data from in order to restore the object
	 * @throws IOException            if I/O errors occur
	 * @throws ClassNotFoundException If the class for an object being
	 *                                restored cannot be found.
	 */
	@GwtIncompatible
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		width = in.readInt();
		height = in.readInt();
		super.readExternal(in);
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
		for (int i = 0; i < vs; i++) {
			Node<Coord> nc = vertexMap.getAt(i);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		DefaultGraph that = (DefaultGraph) o;

		if (width != that.width) return false;
        return height == that.height;
    }

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 107 * result + width;
		result = 107 * result + height;
		return result;
	}

	@Override
	public String toString() {
		return "DefaultGraph: {\n" + String.valueOf(show()) + "\n}";
	}
}
