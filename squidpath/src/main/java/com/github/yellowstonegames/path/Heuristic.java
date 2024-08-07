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
import com.github.yellowstonegames.grid.Coord;

import java.util.Arrays;
import java.util.List;

/** A {@code Heuristic} generates estimates of the cost to move from a given node to the goal.
 * This is a functional interface whose functional method is {@link #estimate(Object, Object)}.
 * <p>
 * With a heuristic function pathfinding algorithms can choose the node that is most likely to lead to the optimal path.
 * The notion of "most likely" is controlled by a heuristic. If the heuristic is accurate, then the algorithm will be
 * efficient. If the heuristic is terrible, then it can perform even worse than other algorithms that don't use any
 * heuristic function such as Dijkstra. SquidSquad's {@link DijkstraMap} is specialized for some cases
 * that A* isn't, so there are reasons to prefer DijkstraMap when, for instance, you have multiple goals, or the goal is
 * unchanging for some section of usage but the start point changes often (this is useful for mouse tracking when the
 * path is reversed). The A-Star code should be significantly faster when paths are short and always have one goal,
 * unless you compare it to DijkstraMap when it can reuse a scan and call
 * {@link DijkstraMap#findPathPreScanned(ObjectDeque, Coord)}
 * 
 * @param <V> Type of vertex; this is usually {@link Coord}
 */
public interface Heuristic<V> {

	/** Calculates an estimated cost to reach the goal node from the given node.
	 * @param node the start node
	 * @param endNode the end node
	 * @return the estimated cost */
	float estimate(V node, V endNode);

	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where diagonal movement is estimated as costing twice as
	 * much as orthogonal movement. This is a good choice for graphs where only four-way movement is used.
	 */
	Heuristic<Coord> MANHATTAN = (node, endNode) -> Math.abs(node.x - endNode.x) + Math.abs(node.y - endNode.y);
	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where diagonal movement is estimated as costing the same as
	 * orthogonal movement. This is only suggested for graphs where eight-way movement is used, and it may produce
	 * erratic paths compared to {@link #EUCLIDEAN}.
	 */
	Heuristic<Coord> CHEBYSHEV = (node, endNode) -> Math.max(Math.abs(node.x - endNode.x), Math.abs(node.y - endNode.y));
	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where all movement is calculated "as-the-crow-flies," using
	 * the standard Pythagorean formula for distance as in the real world. This does not make diagonal connections, if
	 * they are allowed, actually cost more or less, but they won't be preferred if an orthogonal route can be taken.
	 * This is recommended for graphs where eight-way movement is used.
	 */
	Heuristic<Coord> EUCLIDEAN = Coord::distance;
	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where the heuristic is not used, and all cells are
	 * considered equivalent regardless of actual distance.
	 */
	Heuristic<Coord> DIJKSTRA = (node, endNode) -> 0f;
	/**
	 * An unmodifiable List of all the Heuristic implementations in this class.
	 * Contains {@link #MANHATTAN}, {@link #CHEBYSHEV}, {@link #EUCLIDEAN}, and {@link #DIJKSTRA}.
	 */
	List<Heuristic<Coord>> HEURISTICS = Arrays.asList(MANHATTAN, CHEBYSHEV, EUCLIDEAN, DIJKSTRA);
}
